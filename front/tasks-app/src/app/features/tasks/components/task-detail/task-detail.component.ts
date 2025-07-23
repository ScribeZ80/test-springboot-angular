import { Component, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { TaskStatusToggleComponent } from '../../../../shared/components';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, TaskStatusToggleComponent],
  templateUrl: './task-detail.component.html',
  styleUrls: ['./task-detail.component.css']
})
export class TaskDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly taskService = inject(TaskService);
  private readonly _task = signal<Task | null>(null);
  private readonly _isUpdating = signal(false);
  private readonly _isLoading = signal(true);

  task = this._task;
  isUpdating = this._isUpdating;
  isLoading = this._isLoading;

  constructor() {
    effect(() => {
      const id = Number(this.route.snapshot.paramMap.get('id'));
      if (id) {
        this._isLoading.set(true);
        this.taskService.fetchTaskById(id).subscribe({
          next: t => {
            this._task.set(t);
            this._isLoading.set(false);
          },
          error: () => {
            this._task.set(null);
            this._isLoading.set(false);
          }
        });
      }
    });
  }

  toggleStatus(newStatus: boolean) {
    const t = this._task();
    if (!t) return;

    this._isUpdating.set(true);
    this.taskService.updateTaskStatus(t.id, newStatus).subscribe({
      next: (updated) => {
        this._task.set(updated);
        this._isUpdating.set(false);
      },
      error: () => {
        this._isUpdating.set(false);
      }
    });
  }
}
