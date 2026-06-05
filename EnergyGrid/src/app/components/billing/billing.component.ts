import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BillingService } from '../../services/billing.service';
import { TokenService } from '../../services/token.service';
import {
  Invoice, InvoiceRequest, InvoiceStatus, Payment, PaymentRequest,
} from '../../models/billing.models';
import { extractErrorMessage } from '../../utils/error-message';

type FormMode = 'closed' | 'add' | 'edit';

@Component({
  selector: 'app-billing',
  imports: [CommonModule, FormsModule, DatePipe, DecimalPipe],
  templateUrl: './billing.component.html',
  styleUrl: './billing.component.css',
})
export class BillingComponent implements OnInit {
  private billingService = inject(BillingService);
  private tokens = inject(TokenService);

  rows = signal<Invoice[]>([]);
  paymentMethodByInvoice = signal<Map<number, string>>(new Map());
  loading = signal(true);
  loadError = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  isCustomer = computed(() => this.role() === 'CUSTOMER');
  canCreateInvoice = computed(() => this.role() === 'ADMIN');
  canRecordPayment = computed(() => ['ADMIN', 'CUSTOMER'].includes(this.role()));
  canEditInvoice = computed(() => this.role() === 'ADMIN');

  totalCount = computed(() => this.rows().length);
  unpaidCount = computed(() => this.rows().filter((i) => !this.isPaid(i)).length);
  paidCount = computed(() => this.rows().filter((i) => this.isPaid(i)).length);

  payingInvoiceId = signal<number | null>(null);
  payRowError = signal<{ id: number; message: string } | null>(null);

  // Stores the payment method a customer picks for each invoice (keyed by invoice id).
  customerPayMethod: Record<number, string> = {};

  formMode = signal<FormMode>('closed');
  editingId = signal<number | null>(null);
  invoiceSubmitting = signal(false);
  invoiceFormError = signal<string | null>(null);

  invCustomerId = '';
  invDueDate = '';
  invStatus: InvoiceStatus = 'DRAFT';
  invLineDescription = 'Energy usage';
  invQuantity = '';
  invUnitRate = '';
  invPaymentMethod = 'CARD';

  paymentSubmitting = signal(false);
  paymentFormError = signal<string | null>(null);
  paymentFormSuccess = signal<string | null>(null);

  payInvoiceId = '';
  payAmount = '';
  payMethod = 'CARD';

  ngOnInit(): void {
    this.loadInvoices();
  }

  isPaid(invoice: Invoice): boolean {
    return (invoice.status || '').toUpperCase() === 'PAID';
  }

  paymentMethodFor(invoice: Invoice): string {
    return this.paymentMethodByInvoice().get(invoice.id) || '—';
  }

  statusPill(status: string): string {
    const value = (status || '').toUpperCase();
    if (value === 'PAID') return 'pill active';
    if (value === 'SENT') return 'pill scheduled';
    if (value === 'PARTIAL' || value === 'PENDING') return 'pill pending';
    if (value === 'OVERDUE') return 'pill rejected';
    if (value === 'CANCELLED') return 'pill inactive';
    return 'pill muted-pill';
  }

  private loadInvoices(): void {
    this.loading.set(true);
    this.loadError.set(null);

    const userId = this.tokens.user()?.userId;
    let invoices: Invoice[] = [];
    let payments: Payment[] = [];
    let done = 0;

    const finish = () => {
      if (++done < 2) return;
      this.rows.set(invoices);
      this.paymentMethodByInvoice.set(this.buildPaymentMethodMap(invoices, payments));
      // Default each invoice's customer payment method to Card if not chosen yet.
      for (const invoice of invoices) {
        if (!this.customerPayMethod[invoice.id]) {
          this.customerPayMethod[invoice.id] = 'CARD';
        }
      }
      this.loading.set(false);
    };

    const invoices$ = this.isCustomer() && userId
      ? this.billingService.invoicesByCustomer(userId)
      : this.billingService.listInvoices();

    invoices$.subscribe({
      next: (result) => { invoices = result || []; finish(); },
      error: (err) => {
        this.loadError.set(extractErrorMessage(err, 'Failed to load invoices.'));
        finish();
      },
    });

    const payments$ = this.isCustomer() && userId
      ? this.billingService.paymentsByCustomer(userId)
      : this.billingService.listPayments();

    payments$.subscribe({
      next: (result) => { payments = result || []; finish(); },
      error: () => finish(),
    });
  }

  private buildPaymentMethodMap(invoices: Invoice[], payments: Payment[]): Map<number, string> {
    const map = new Map<number, string>();
    const sortedPayments = [...payments].sort(
      (a, b) => (a.paymentDate || '').localeCompare(b.paymentDate || ''),
    );
    for (const payment of sortedPayments) {
      if (payment.invoiceId && payment.paymentMethod) {
        map.set(payment.invoiceId, payment.paymentMethod);
      }
    }
    for (const invoice of invoices) {
      const method = (invoice as any).paymentMethod;
      if (method) map.set(invoice.id, method);
    }
    return map;
  }

  openCreateInvoice(): void {
    this.invCustomerId = '';
    this.invDueDate = '';
    this.invStatus = 'DRAFT';
    this.invLineDescription = 'Energy usage';
    this.invQuantity = '';
    this.invUnitRate = '';
    this.invPaymentMethod = 'CARD';
    this.editingId.set(null);
    this.invoiceFormError.set(null);
    this.formMode.set('add');
  }

  openEditInvoice(invoice: Invoice): void {
    const firstLine = invoice.lineItems?.[0];
    this.invCustomerId = String(invoice.customerId);
    this.invDueDate = invoice.dueDate || '';
    this.invStatus = invoice.status as InvoiceStatus;
    this.invLineDescription = firstLine?.description || 'Energy usage';
    this.invQuantity = firstLine?.quantity != null ? String(firstLine.quantity) : '';
    this.invUnitRate = firstLine?.unitRate != null ? String(firstLine.unitRate) : '';
    this.invPaymentMethod = this.paymentMethodByInvoice().get(invoice.id) || 'CARD';
    this.editingId.set(invoice.id);
    this.invoiceFormError.set(null);
    this.formMode.set('edit');
  }

  closeInvoiceForm(): void {
    this.formMode.set('closed');
  }

  submitInvoice(): void {
    this.invoiceFormError.set(null);
    if (this.formMode() === 'edit') {
      this.updateInvoiceStatusOnly();
    } else {
      this.createNewInvoice();
    }
  }

  private updateInvoiceStatusOnly(): void {
    const id = this.editingId();
    if (id === null) return;
    this.invoiceSubmitting.set(true);
    this.billingService.updateInvoiceStatus(id, this.invStatus).subscribe({
      next: () => this.finishInvoiceSave(),
      error: (err) => this.failInvoiceSave(err, 'Save failed.'),
    });
  }

  private createNewInvoice(): void {
    const customerId = Number(this.invCustomerId);
    const quantity = Number(this.invQuantity);
    const unitRate = Number(this.invUnitRate);

    if (!isFinite(customerId) || customerId <= 0) {
      this.invoiceFormError.set('Customer ID required.');
      return;
    }
    if (!isFinite(quantity) || quantity <= 0) {
      this.invoiceFormError.set('Quantity must be > 0.');
      return;
    }
    if (!isFinite(unitRate) || unitRate <= 0) {
      this.invoiceFormError.set('Unit rate must be > 0.');
      return;
    }

    const body: InvoiceRequest = {
      customerId,
      dueDate: this.invDueDate || undefined,
      lineItems: [{
        description: (this.invLineDescription || 'Energy usage').trim(),
        quantity,
        unitRate,
      }],
    };

    this.invoiceSubmitting.set(true);
    this.billingService.createInvoice(body).subscribe({
      next: (created) => this.maybeUpdateStatus(created.id),
      error: (err) => this.failInvoiceSave(err, 'Save failed.'),
    });
  }

  private maybeUpdateStatus(invoiceId: number): void {
    if (!this.invStatus || this.invStatus === 'DRAFT') {
      this.finishInvoiceSave();
      return;
    }
    this.billingService.updateInvoiceStatus(invoiceId, this.invStatus).subscribe({
      next: () => this.finishInvoiceSave(),
      error: (err) => this.failInvoiceSave(err, 'Invoice saved, status update failed.'),
    });
  }

  private finishInvoiceSave(): void {
    this.invoiceSubmitting.set(false);
    this.formMode.set('closed');
    this.loadInvoices();
  }

  private failInvoiceSave(err: any, fallback: string): void {
    this.invoiceFormError.set(extractErrorMessage(err, fallback));
    this.invoiceSubmitting.set(false);
  }

  markPaid(invoice: Invoice): void {
    if (this.payingInvoiceId() === invoice.id || this.isPaid(invoice)) return;
    this.payRowError.set(null);
    this.payingInvoiceId.set(invoice.id);

    // Use the payment method the customer chose for this invoice (default to Card).
    const method = this.customerPayMethod[invoice.id] || 'CARD';
    const body: PaymentRequest = {
      invoiceId: invoice.id,
      amount: Number(invoice.amount) || 0,
      paymentMethod: method,
    };

    this.billingService.recordPayment(body).subscribe({
      next: () => {
        this.payingInvoiceId.set(null);
        this.loadInvoices();
      },
      error: (err) => {
        this.payingInvoiceId.set(null);
        this.showPayRowError(invoice.id, extractErrorMessage(err, 'Payment failed. Please try again.'));
      },
    });
  }

  private showPayRowError(invoiceId: number, message: string): void {
    this.payRowError.set({ id: invoiceId, message });
    setTimeout(() => {
      if (this.payRowError()?.id === invoiceId) {
        this.payRowError.set(null);
      }
    }, 5000);
  }

  submitPayment(): void {
    this.paymentFormError.set(null);
    this.paymentFormSuccess.set(null);

    const invoiceId = Number(this.payInvoiceId);
    const amount = Number(this.payAmount);

    if (!isFinite(invoiceId) || invoiceId <= 0) {
      this.paymentFormError.set('Invoice ID required.');
      return;
    }
    if (!isFinite(amount) || amount <= 0) {
      this.paymentFormError.set('Amount must be > 0.');
      return;
    }
    if (!this.payMethod) {
      this.paymentFormError.set('Payment method required.');
      return;
    }

    const body: PaymentRequest = { invoiceId, amount, paymentMethod: this.payMethod };
    this.paymentSubmitting.set(true);

    this.billingService.recordPayment(body).subscribe({
      next: () => {
        this.paymentSubmitting.set(false);
        this.paymentFormSuccess.set('Payment recorded.');
        this.payInvoiceId = '';
        this.payAmount = '';
        this.loadInvoices();
      },
      error: (err) => {
        this.paymentFormError.set(extractErrorMessage(err, 'Payment failed.'));
        this.paymentSubmitting.set(false);
      },
    });
  }
}
