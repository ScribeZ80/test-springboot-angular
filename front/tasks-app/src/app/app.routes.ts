import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadChildren: () => import('./features/tasks/tasks.module').then(m => m.TasksModule) },
];
