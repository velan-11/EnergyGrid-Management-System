import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Invoice, InvoiceRequest, Payment, PaymentRequest } from '../models/billing.models';

@Injectable({ providedIn: 'root' })
export class BillingService {
  private http = inject(HttpClient);
  private invoicesUrl = environment.apiUrl + '/api/invoices';
  private paymentsUrl = environment.apiUrl + '/api/payments';

  createInvoice(body: InvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.invoicesUrl, body);
  }

  listInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(this.invoicesUrl);
  }

  invoicesByCustomer(customerId: number): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(this.invoicesUrl + '/customer/' + customerId);
  }

  getInvoice(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(this.invoicesUrl + '/' + id);
  }

  updateInvoiceStatus(id: number, status: string): Observable<Invoice> {
    return this.http.patch<Invoice>(this.invoicesUrl + '/' + id + '/status', { status });
  }

  recordPayment(body: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(this.paymentsUrl, body);
  }

  listPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.paymentsUrl);
  }

  paymentsByCustomer(customerId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.paymentsUrl + '/customer/' + customerId);
  }

  paymentsByInvoice(invoiceId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.paymentsUrl + '/invoice/' + invoiceId);
  }
}
