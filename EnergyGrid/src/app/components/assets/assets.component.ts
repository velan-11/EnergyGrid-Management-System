import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AssetService } from '../../services/asset.service';
import { TokenService } from '../../services/token.service';
import {
  Asset, AssetRequest, assetCapacity, assetDisplayName, assetId,
  assetLocation, assetName, assetStatus, assetType,
} from '../../models/asset.models';
import { extractErrorMessage } from '../../utils/error-message';

type Mode = 'closed' | 'add' | 'edit';

@Component({
  selector: 'app-assets',
  imports: [CommonModule, FormsModule],
  templateUrl: './assets.component.html',
  styleUrl: './assets.component.css',
})
export class AssetsComponent implements OnInit {
  private svc = inject(AssetService);
  private tokens = inject(TokenService);

  rows = signal<Asset[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  canCreate = computed(() => ['ADMIN', 'OPERATOR', 'PRODUCER'].includes(this.role()));
  canEdit = computed(() => ['ADMIN', 'OPERATOR', 'PRODUCER'].includes(this.role()));
  canDelete = computed(() => ['ADMIN', 'OPERATOR'].includes(this.role()));
  canWrite = computed(() => this.canCreate() || this.canEdit() || this.canDelete());

  mode = signal<Mode>('closed');
  editingId = signal<number | null>(null);
  submitting = signal(false);
  formError = signal<string | null>(null);

  name = '';
  type: any = 'Solar';
  location = '';
  capacityKW = '';
  status: any = 'ACTIVE';

  id = assetId;
  nameOf = assetName;
  displayNameOf = assetDisplayName;
  typeOf = assetType;
  locationOf = assetLocation;
  capacityOf = assetCapacity;
  statusOf = assetStatus;

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.list().subscribe({
      next: (rows) => {
        this.rows.set(rows || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Failed to load assets.'));
        this.loading.set(false);
      },
    });
  }

  openAdd(): void {
    this.resetForm();
    this.mode.set('add');
  }

  openEdit(asset: Asset): void {
    this.name = assetName(asset);
    this.type = assetType(asset) || 'Solar';
    this.location = assetLocation(asset);
    this.capacityKW = String(assetCapacity(asset) || '');
    this.status = assetStatus(asset) || 'ACTIVE';
    this.editingId.set(assetId(asset));
    this.formError.set(null);
    this.mode.set('edit');
  }

  cancel(): void {
    this.mode.set('closed');
    this.resetForm();
  }

  submit(): void {
    const capacity = Number(this.capacityKW);
    if (!this.name.trim()) { this.formError.set('Name is required.'); return; }
    if (!this.location.trim()) { this.formError.set('Location is required.'); return; }
    if (!isFinite(capacity) || capacity <= 0) {
      this.formError.set('Capacity must be greater than zero.');
      return;
    }

    const body: AssetRequest = {
      Name: this.name.trim(),
      Type: this.type,
      Location: this.location.trim(),
      CapacityKW: capacity,
      Status: this.status,
    };

    this.submitting.set(true);
    this.formError.set(null);

    const request$ = this.mode() === 'edit' && this.editingId() !== null
      ? this.svc.update(this.editingId()!, body)
      : this.svc.create(body);

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.mode.set('closed');
        this.resetForm();
        this.load();
      },
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Save failed.'));
        this.submitting.set(false);
      },
    });
  }

  delete(asset: Asset): void {
    const label = assetName(asset) || '#' + assetId(asset);
    if (!confirm('Delete asset "' + label + '"? This cannot be undone.')) return;
    this.svc.delete(assetId(asset)).subscribe({
      next: () => this.load(),
      error: (err) => this.error.set(extractErrorMessage(err, 'Delete failed.')),
    });
  }

  private resetForm(): void {
    this.name = '';
    this.type = 'Solar';
    this.location = '';
    this.capacityKW = '';
    this.status = 'ACTIVE';
    this.editingId.set(null);
    this.formError.set(null);
  }
}
