import { inject, Component, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { Task, PageResponse } from '../../models/task.model';
import { RouterLink } from '@angular/router';
import { TaskStatusToggleComponent } from '../../../../shared/components';
import { AddTaskComponent } from '../add-task/add-task.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TaskStatusToggleComponent, AddTaskComponent],
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent {
  private readonly taskService = inject(TaskService);
  private readonly _tasks = signal<Task[]>([]);
  private readonly _pageInfo = signal<Omit<PageResponse<Task>, 'content'> | null>(null);
  private readonly _page = signal(0);
  private readonly _size = 12;
  private readonly _status = signal<'all' | 'completed' | 'pending'>('all');
  private readonly _updatingTasks = signal<Set<number>>(new Set());
  // Signal pour déclencher le rechargement
  private readonly _refreshTrigger = signal(0);

  tasks = computed(() => this._tasks());
  pageInfo = computed(() => this._pageInfo());
  currentStatus = computed(() => this._status());

  constructor() {
    effect(() => {
      const status = this._status();
      const page = this._page();
      const refreshTrigger = this._refreshTrigger(); // Inclure le trigger dans l'effect
      
      let completed: boolean | undefined = undefined;
      if (status === 'completed') completed = true;
      if (status === 'pending') completed = false;
      
      // Utiliser le endpoint spécifique pour les tâches en cours
      if (status === 'pending') {
        this.taskService.fetchPendingTasks(page, this._size, 'id,desc').subscribe({
          next: (page: PageResponse<Task>) => {
            this._tasks.set(page.content);
            const { content: _, ...info } = page;
            this._pageInfo.set(info);
          },
          error: () => {
            this._tasks.set([]);
            this._pageInfo.set(null);
          }
        });
      } else {
        this.taskService.fetchTasks(page, this._size, 'id,desc', completed).subscribe({
          next: (page: PageResponse<Task>) => {
            this._tasks.set(page.content);
            const { content: _, ...info } = page;
            this._pageInfo.set(info);
          },
          error: () => {
            this._tasks.set([]);
            this._pageInfo.set(null);
          }
        });
      }
    });
  }

  onStatusChange(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    this._status.set(value as 'all' | 'completed' | 'pending');
    this._page.set(0); // reset page
  }

  nextPage() {
    if (this._pageInfo()?.hasNext) {
      this._page.set(this._page() + 1);
    }
  }

  prevPage() {
    if (this._pageInfo()?.hasPrevious && this._page() > 0) {
      this._page.set(this._page() - 1);
    }
  }

  toggleStatus(task: Task, newStatus: boolean) {
    this._updatingTasks.update(tasks => new Set(tasks).add(task.id));

    this.taskService.updateTaskStatus(task.id, newStatus).subscribe({
      next: (updated) => {
        this._tasks.set(this._tasks().map(t => t.id === updated.id ? updated : t));
        this._updatingTasks.update(tasks => {
          const newSet = new Set(tasks);
          newSet.delete(task.id);
          return newSet;
        });
      },
      error: () => {
        this._updatingTasks.update(tasks => {
          const newSet = new Set(tasks);
          newSet.delete(task.id);
          return newSet;
        });
      }
    });
  }

  isUpdating(taskId: number): boolean {
    return this._updatingTasks().has(taskId);
  }

  trackById(index: number, task: Task) {
    return task.id;
  }

  onTaskCreated() {
    // Solution propre : déclencher l'effect via le signal
    this._page.set(0); // Reset to first page
    this._refreshTrigger.update(trigger => trigger + 1); // Force effect to run
  }
}
