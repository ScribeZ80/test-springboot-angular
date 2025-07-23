import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-task-status-toggle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task-status-toggle.component.html',
  styleUrls: ['./task-status-toggle.component.css']
})
export class TaskStatusToggleComponent {
  // Inputs
  isCompleted = input.required<boolean>();
  isUpdating = input<boolean>(false);
  size = input<'small' | 'medium' | 'large'>('medium');

  // Outputs
  statusChange = output<boolean>();

  onToggle() {
    if (!this.isUpdating()) {
      this.statusChange.emit(!this.isCompleted());
    }
  }

  getAriaLabel(): string {
    const action = this.isCompleted() ? 'marquer comme en cours' : 'marquer comme terminée';
    const state = this.isUpdating() ? ' (mise à jour en cours)' : '';
    return `${action}${state}`;
  }
}
