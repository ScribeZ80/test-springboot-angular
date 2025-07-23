import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { CreateTaskRequest } from '../../models/task.model';

@Component({
  selector: 'app-add-task',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-task.component.html',
  styleUrls: ['./add-task.component.css']
})
export class AddTaskComponent {
  @Output() taskCreated = new EventEmitter<void>();

  private readonly taskService = inject(TaskService);

  label = '';
  description = '';
  loading = signal(false);

  onSubmit() {
    if (!this.label.trim() || !this.description.trim()) return;
    this.loading.set(true);
    const request: CreateTaskRequest = {
      label: this.label.trim(),
      description: this.description.trim(),
    };
    this.taskService.createTask(request).subscribe({
      next: () => {
        this.label = '';
        this.description = '';
        this.loading.set(false);
        this.taskCreated.emit();
      },
      error: () => {
        this.loading.set(false);
        console.error('Erreur lors de l\'ajout de la tâche');
      }
    });
  }
}
