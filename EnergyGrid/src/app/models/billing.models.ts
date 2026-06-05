export type InvoiceStatus = 'DRAFT' | 'SENT' | 'PAID' | 'PARTIAL' | 'OVERDUE' | 'CANCELLED';
export type PaymentStatus = 'SUCCESS' | 'FAILED' | 'PENDING';

export interface LineItem {
  id?: number;
  description: string;
  quantity: number;
  unitRate: number;
  amount: number;
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  customerId: number;
  periodStart?: string | null;
  periodEnd?: string | null;
  subtotal: number;
  tax: number;
  amount: number;
  status: string;
  dueDate?: string | null;
  paidAt?: string | null;
  createdAt: string;
  lineItems: LineItem[];
}

export interface InvoiceLineItemRequest {
  description: string;
  quantity: number;
  unitRate: number;
}

export interface InvoiceRequest {
  customerId: number;
  periodStart?: string;
  periodEnd?: string;
  dueDate?: string;
  taxRate?: number;
  lineItems?: InvoiceLineItemRequest[];
  energyUsed?: number;
  unitPrice?: number;
}

export interface Payment {
  id: number;
  invoiceId: number;
  customerId?: number;
  amount: number;
  paymentMethod: string;
  transactionId?: string;
  paymentDate: string;
  status: string;
}

export interface PaymentRequest {
  invoiceId: number;
  amount: number;
  paymentMethod: string;
}
