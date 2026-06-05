import {
  Component, ElementRef, OnInit, ViewChild, computed, inject, signal,
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OutageService } from '../../services/outage.service';
import { AssetService } from '../../services/asset.service';
import { WorkOrderService } from '../../services/workorder.service';
import { TokenService } from '../../services/token.service';
import { IncidentTask, IncidentTaskRequest, IncidentStatus, Outage } from '../../models/outage.models';
import { Asset, assetId, assetName, assetType } from '../../models/asset.models';
import { extractErrorMessage } from '../../utils/error-message';

type FormMode = 'closed' | 'add' | 'edit';

interface RowVm extends IncidentTask {
  outageAsset: string;
}

@Component({
  selector: 'app-incident-tasks',
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './incident-tasks.component.html',
  styleUrl: './incident-tasks.component.css',
})
export class IncidentTasksComponent implements OnInit {
  private outageService = inject(OutageService);
  private assetService = inject(AssetService);
  private workOrderService = inject(WorkOrderService);
  private tokens = inject(TokenService);

  rows = signal<RowVm[]>([]);
  outages = signal<Outage[]>([]);
  assets = signal<Asset[]>([]);
  outageById = signal<Map<number, Outage>>(new Map());
  assetById = signal<Map<number, Asset>>(new Map());
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  canWrite = computed(() => ['ADMIN', 'OPERATOR'].includes(this.role()));

  mode = signal<FormMode>('closed');
  editingId = signal<number | null>(null);
  submitting = signal(false);
  formError = signal<string | null>(null);
  evidenceWarning = signal<string | null>(null);

  outageId = '';
  assignedTo = '';
  status: IncidentStatus = 'ASSIGNED';
  currentEvidenceUri = '';
  pendingFile: File | null = null;
  previewUrl: string | null = null;

  @ViewChild('rowFileInput') private rowFileInput?: ElementRef<HTMLInputElement>;
  activeUploadRowId = signal<number | null>(null);
  uploadingId = signal<number | null>(null);
  justUploadedId = signal<number | null>(null);
  uploadErrorByRow = signal<Map<number, string>>(new Map());

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    let tasks: IncidentTask[] | null = null;
    let outages: Outage[] | null = null;
    let assets: Asset[] | null = null;
    let done = 0;

    const tryBuild = () => {
      if (++done < 3) return;
      const o = outages || [];
      const a = assets || [];
      this.outages.set(o);
      this.assets.set(a);
      this.outageById.set(this.indexOutages(o));
      this.assetById.set(this.indexAssets(a));
      this.rows.set(this.toRowVms(tasks || []));
      this.loading.set(false);
    };

    this.outageService.listIncidents().subscribe({
      next: (result) => { tasks = result; tryBuild(); },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err, 'Failed to load incident tasks.'));
        tryBuild();
      },
    });

    this.outageService.listOutages().subscribe({
      next: (result) => { outages = result; tryBuild(); },
      error: () => tryBuild(),
    });

    this.assetService.list().subscribe({
      next: (result) => { assets = result; tryBuild(); },
      error: () => tryBuild(),
    });
  }

  private indexOutages(outages: Outage[]): Map<number, Outage> {
    const lookup = new Map<number, Outage>();
    for (const outage of outages) lookup.set(outage.id, outage);
    return lookup;
  }

  private indexAssets(assets: Asset[]): Map<number, Asset> {
    const lookup = new Map<number, Asset>();
    for (const asset of assets) lookup.set(assetId(asset), asset);
    return lookup;
  }

  private toRowVms(tasks: IncidentTask[]): RowVm[] {
    return tasks.map((task) => ({
      ...task,
      outageAsset: this.outageAssetLabel(task.outageId),
    }));
  }

  private outageAssetLabel(outageId: number): string {
    const outage = this.outageById().get(outageId);
    if (!outage) return '—';
    const ids = this.parseAssetIds(outage.affectedAssetsJSON);
    if (ids.length === 0) return '—';
    return this.formatAssetLabel(ids[0]);
  }

  private formatAssetLabel(idValue: number): string {
    const asset = this.assetById().get(idValue);
    if (!asset) return 'Asset #' + idValue;
    return assetName(asset) || ((assetType(asset) || 'Asset') + ' #' + idValue);
  }

  private parseAssetIds(raw: any): number[] {
    if (!raw) return [];
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed.map(Number).filter(isFinite);
      return [];
    } catch {
      return [];
    }
  }

  outageDisplay(outage: Outage): string {
    const ids = this.parseAssetIds(outage.affectedAssetsJSON);
    const label = ids.length === 0 ? 'unassigned asset' : this.formatAssetLabel(ids[0]);
    return 'Outage #' + outage.id + ' — ' + label;
  }

  openAdd(): void {
    this.resetForm();
    this.mode.set('add');
  }

  openEdit(task: RowVm): void {
    this.outageId = String(task.outageId);
    this.assignedTo = String(task.assignedTo);
    this.status = (task.status as IncidentStatus) || 'ASSIGNED';
    this.currentEvidenceUri = task.evidenceURI || '';
    this.previewUrl = this.isImageUri(this.currentEvidenceUri) ? this.currentEvidenceUri : null;
    this.pendingFile = null;
    this.editingId.set(task.id);
    this.formError.set(null);
    this.evidenceWarning.set(null);
    this.mode.set('edit');
  }

  cancel(): void {
    this.mode.set('closed');
    this.resetForm();
  }

  onFilePicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.pendingFile = file;
    this.releaseBlobPreview();
    this.previewUrl = file.type.startsWith('image/') ? URL.createObjectURL(file) : null;
  }

  private releaseBlobPreview(): void {
    if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.previewUrl);
    }
  }

  submit(): void {
    this.formError.set(null);
    this.evidenceWarning.set(null);

    if (!this.isFormValid()) return;
    this.submitting.set(true);

    if (this.pendingFile) {
      this.uploadEvidenceThenSubmit();
    } else {
      this.submitWithUri(this.currentEvidenceUri || 'about:blank');
    }
  }

  private isFormValid(): boolean {
    const outageNumber = Number(this.outageId);
    const assigneeNumber = Number(this.assignedTo);
    if (!isFinite(outageNumber) || outageNumber <= 0) {
      this.formError.set('Outage is required.');
      return false;
    }
    if (!isFinite(assigneeNumber) || assigneeNumber <= 0) {
      this.formError.set('Assigned To (user id) is required.');
      return false;
    }
    return true;
  }

  private uploadEvidenceThenSubmit(): void {
    this.workOrderService.uploadFile(this.pendingFile!).subscribe({
      next: (res) => this.submitWithUri(res.fileUrl),
      error: (err) => {
        if (this.mode() === 'edit') {
          this.evidenceWarning.set('Evidence upload failed — task saved without evidence.');
          this.submitWithUri(this.currentEvidenceUri || '');
        } else {
          this.formError.set(extractErrorMessage(err, 'Evidence upload failed.'));
          this.submitting.set(false);
        }
      },
    });
  }

  private submitWithUri(uri: string): void {
    if (this.mode() === 'edit' && this.editingId() !== null) {
      this.updateExistingTask(uri);
    } else {
      this.createNewTask(uri);
    }
  }

  private updateExistingTask(uri: string): void {
    const id = this.editingId()!;
    this.outageService.updateIncidentStatus(id, this.status).subscribe({
      next: () => this.maybeUpdateEvidence(id, uri),
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Status update failed.'));
        this.submitting.set(false);
      },
    });
  }

  private maybeUpdateEvidence(id: number, uri: string): void {
    if (!uri || uri === this.currentEvidenceUri) {
      this.finish();
      return;
    }
    this.outageService.updateIncidentEvidence(id, uri).subscribe({
      next: () => this.finish(),
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Evidence update failed.'));
        this.submitting.set(false);
      },
    });
  }

  private createNewTask(uri: string): void {
    const body: IncidentTaskRequest = {
      outageId: Number(this.outageId),
      assignedTo: Number(this.assignedTo),
      evidenceURI: uri,
      status: this.status,
    };
    this.outageService.createIncident(body).subscribe({
      next: () => this.finish(),
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Create failed.'));
        this.submitting.set(false);
      },
    });
  }

  private finish(): void {
    this.submitting.set(false);
    this.mode.set('closed');
    this.resetForm();
    this.load();
  }

  canUploadFor(task: RowVm): boolean {
    // Admin, Operator and Technician can all upload evidence from the Evidence column.
    const userRole = this.role();
    return userRole === 'ADMIN' || userRole === 'OPERATOR' || userRole === 'TECHNICIAN';
  }

  isImageUri(uri: any): boolean {
    if (!uri) return false;
    if (typeof uri !== 'string') return false;
    if (uri.startsWith('data:image/')) return true;
    return /\.(jpe?g|png|gif|webp|bmp|svg)(\?.*)?$/i.test(uri);
  }

  startUploadFor(task: RowVm, event: Event): void {
    event.stopPropagation();
    if (!this.canUploadFor(task)) return;
    this.activeUploadRowId.set(task.id);
    const fileInput = this.rowFileInput?.nativeElement;
    if (fileInput) {
      fileInput.value = '';
      fileInput.click();
    }
  }

  onRowFilePicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const rowId = this.activeUploadRowId();
    this.activeUploadRowId.set(null);
    if (!file || rowId === null) return;

    this.clearRowError(rowId);
    this.uploadingId.set(rowId);

    this.workOrderService.uploadFile(file).subscribe({
      next: (res) => {
        const uri = res?.fileUrl;
        if (!uri) {
          this.failRow(rowId, 'Upload failed. Try again.');
          return;
        }
        this.outageService.updateIncidentEvidence(rowId, uri).subscribe({
          next: () => this.succeedRow(rowId, uri),
          error: () => this.failRow(rowId, 'Upload failed. Try again.'),
        });
      },
      error: () => this.failRow(rowId, 'Upload failed. Try again.'),
    });
  }

  private succeedRow(rowId: number, uri: string): void {
    this.rows.update((list) =>
      list.map((row) => row.id === rowId ? { ...row, evidenceURI: uri } : row),
    );
    this.uploadingId.set(null);
    this.justUploadedId.set(rowId);
    setTimeout(() => {
      if (this.justUploadedId() === rowId) this.justUploadedId.set(null);
    }, 2000);
  }

  private failRow(rowId: number, message: string): void {
    this.uploadingId.set(null);
    this.uploadErrorByRow.update((m) => {
      const next = new Map(m);
      next.set(rowId, message);
      return next;
    });
    setTimeout(() => this.clearRowError(rowId), 4000);
  }

  private clearRowError(rowId: number): void {
    this.uploadErrorByRow.update((m) => {
      if (!m.has(rowId)) return m;
      const next = new Map(m);
      next.delete(rowId);
      return next;
    });
  }

  rowError(id: number): string | null {
    return this.uploadErrorByRow().get(id) || null;
  }

  private resetForm(): void {
    this.outageId = '';
    this.assignedTo = '';
    this.status = 'ASSIGNED';
    this.currentEvidenceUri = '';
    this.pendingFile = null;
    this.releaseBlobPreview();
    this.previewUrl = null;
    this.editingId.set(null);
    this.formError.set(null);
    this.evidenceWarning.set(null);
  }
}
