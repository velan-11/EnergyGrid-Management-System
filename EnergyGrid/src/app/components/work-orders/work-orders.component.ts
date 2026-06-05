import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WorkOrderService } from '../../services/workorder.service';
import { AssetService } from '../../services/asset.service';
import { AdminService } from '../../services/admin.service';
import { TokenService } from '../../services/token.service';
import { WorkOrder, WorkOrderRequest } from '../../models/workorder.models';
import { AdminUser } from '../../models/auth.models';
import { Asset, assetId, assetName, assetNameWithType, assetType } from '../../models/asset.models';
import { extractErrorMessage } from '../../utils/error-message';

type FormMode = 'closed' | 'add' | 'edit';

@Component({
  selector: 'app-work-orders',
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './work-orders.component.html',
  styleUrl: './work-orders.component.css',
})
export class WorkOrdersComponent implements OnInit {
  private workOrderService = inject(WorkOrderService);
  private assetService = inject(AssetService);
  private adminService = inject(AdminService);
  private tokens = inject(TokenService);

  rows = signal<WorkOrder[]>([]);
  assets = signal<Asset[]>([]);
  assetById = signal<Map<number, Asset>>(new Map());
  technicians = signal<AdminUser[]>([]);
  technicianById = signal<Map<number, AdminUser>>(new Map());
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  canWrite = computed(() => this.role() === 'ADMIN');

  mode = signal<FormMode>('closed');
  editingId = signal<number | null>(null);
  submitting = signal(false);
  formError = signal<string | null>(null);

  assetIdInput = '';
  issueDescription = '';
  dueDate = '';
  status = 'OPEN';
  assignedTechnicianId = '';

  aId = assetId;
  aName = assetName;
  aType = assetType;
  aLabel = assetNameWithType;

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    // Only admins can read the user list, so non-admins make 2 calls, not 3.
    // This avoids the 403 "Access Denied" toast for technicians/other roles.
    const totalCalls = this.canWrite() ? 3 : 2;
    let done = 0;
    const finish = () => { if (++done === totalCalls) this.loading.set(false); };

    this.workOrderService.list().subscribe({
      next: (orders) => { this.rows.set(orders || []); finish(); },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err, 'Failed to load work orders.'));
        finish();
      },
    });

    this.assetService.list().subscribe({
      next: (assets) => {
        this.assets.set(assets || []);
        this.assetById.set(this.indexAssets(assets || []));
        finish();
      },
      error: () => finish(),
    });

    // The technician list is only needed for the admin's assign/edit form.
    if (this.canWrite()) {
      this.adminService.list().subscribe({
        next: (users) => {
          const techs = this.pickActiveTechnicians(users || []);
          this.technicians.set(techs);
          this.technicianById.set(this.indexTechnicians(techs));
          finish();
        },
        error: () => finish(),
      });
    }
  }

  private indexAssets(assets: Asset[]): Map<number, Asset> {
    const lookup = new Map<number, Asset>();
    for (const asset of assets) lookup.set(assetId(asset), asset);
    return lookup;
  }

  private pickActiveTechnicians(users: AdminUser[]): AdminUser[] {
    return users.filter((u) =>
      (u.role || '').toUpperCase() === 'TECHNICIAN'
      && (u.status || '').toUpperCase() === 'ACTIVE'
      && !u.deleted,
    );
  }

  private indexTechnicians(techs: AdminUser[]): Map<number, AdminUser> {
    const lookup = new Map<number, AdminUser>();
    for (const tech of techs) lookup.set(tech.userId, tech);
    return lookup;
  }

  assetLabel(idValue: number): string {
    const asset = this.assetById().get(idValue);
    if (!asset) return 'Asset #' + idValue;
    return assetNameWithType(asset);
  }

  assignedDisplay(order: WorkOrder): string {
    if (order.technician?.name?.trim()) {
      return order.technician.name.trim();
    }
    if (order.technician?.id) {
      const tech = this.technicianById().get(order.technician.id);
      return tech?.name || 'Technician #' + order.technician.id;
    }
    return 'Unassigned';
  }

  openAdd(): void {
    this.resetForm();
    this.mode.set('add');
  }

  openEdit(order: WorkOrder): void {
    this.assetIdInput = String(order.assetId);
    this.issueDescription = order.issueDescription || '';
    this.dueDate = this.ensureFutureLocalInput(order.dueDate || '');
    this.status = order.status || 'OPEN';
    this.assignedTechnicianId = order.technician?.id ? String(order.technician.id) : '';
    this.editingId.set(order.id);
    this.formError.set(null);
    this.mode.set('edit');
  }

  private ensureFutureLocalInput(raw: string): string {
    const oneMinuteFromNow = Date.now() + 60000;
    let date: Date | null = null;
    if (raw) {
      const parsed = new Date(raw);
      if (!isNaN(parsed.getTime())) date = parsed;
    }
    if (!date || date.getTime() <= oneMinuteFromNow) {
      date = new Date(Date.now() + 24 * 60 * 60 * 1000);
    }
    return this.toLocalInput(date);
  }

  private toLocalInput(date: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
      + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  }

  cancel(): void {
    this.mode.set('closed');
  }

  submit(): void {
    const body = this.buildValidBody();
    if (!body) return;

    this.submitting.set(true);
    this.formError.set(null);

    const request$ = this.mode() === 'edit' && this.editingId() !== null
      ? this.workOrderService.update(this.editingId()!, body)
      : this.workOrderService.create(body);

    request$.subscribe({
      next: (saved) => this.applyAssignmentAndStatus(saved),
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Save failed.'));
        this.submitting.set(false);
      },
    });
  }

  private buildValidBody(): WorkOrderRequest | null {
    const assetIdNumber = Number(this.assetIdInput);
    const description = this.issueDescription.trim();

    if (!isFinite(assetIdNumber) || assetIdNumber <= 0) {
      this.formError.set('Asset is required.');
      return null;
    }
    if (!description) {
      this.formError.set('Description is required.');
      return null;
    }
    if (description.length < 5) {
      this.formError.set('Description must be at least 5 characters.');
      return null;
    }
    if (description.length > 200) {
      this.formError.set('Description must be 200 characters or fewer.');
      return null;
    }
    if (!this.dueDate) {
      this.formError.set('Due date is required.');
      return null;
    }
    if (Date.parse(this.dueDate) <= Date.now()) {
      this.formError.set('Due date must be in the future.');
      return null;
    }

    return {
      assetId: assetIdNumber,
      issueDescription: description,
      dueDate: this.dueDate.length === 16 ? this.dueDate + ':00' : this.dueDate,
    };
  }

  private applyAssignmentAndStatus(saved: WorkOrder): void {
    const orderId = saved?.id || this.editingId();
    if (orderId === null) {
      this.finishSave();
      return;
    }

    const techId = Number(this.assignedTechnicianId);
    const savedTechId = saved?.technician?.id || -1;
    const shouldAssign = isFinite(techId) && techId > 0 && techId !== savedTechId;
    const shouldChangeStatus = !!this.status && this.status !== (saved?.status || '');

    if (shouldAssign) {
      this.runAssign(orderId, techId, () => this.maybeUpdateStatus(orderId, shouldChangeStatus));
    } else {
      this.maybeUpdateStatus(orderId, shouldChangeStatus);
    }
  }

  private runAssign(orderId: number, techId: number, onSuccess: () => void): void {
    const techName = this.technicianById().get(techId)?.name || null;
    this.workOrderService.assign(orderId, techId, techName).subscribe({
      next: () => onSuccess(),
      error: (err) => {
        const message = extractErrorMessage(err, 'Technician could not be assigned.');
        this.formError.set('Work order saved, but assignment failed: ' + message);
        this.submitting.set(false);
        this.load();
      },
    });
  }

  private maybeUpdateStatus(orderId: number, shouldChangeStatus: boolean): void {
    if (!shouldChangeStatus) {
      this.finishSave();
      return;
    }
    this.workOrderService.updateStatus(orderId, this.status).subscribe({
      next: () => this.finishSave(),
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Status change failed (work order saved).'));
        this.submitting.set(false);
        this.load();
      },
    });
  }

  private finishSave(): void {
    this.submitting.set(false);
    this.mode.set('closed');
    this.load();
  }

  delete(order: WorkOrder): void {
    if (!confirm('Delete work order #' + order.id + '?')) return;
    this.workOrderService.delete(order.id).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage.set(extractErrorMessage(err, 'Delete failed.')),
    });
  }

  private resetForm(): void {
    this.assetIdInput = '';
    this.issueDescription = '';
    this.dueDate = '';
    this.status = 'OPEN';
    this.assignedTechnicianId = '';
    this.editingId.set(null);
    this.formError.set(null);
  }
}
