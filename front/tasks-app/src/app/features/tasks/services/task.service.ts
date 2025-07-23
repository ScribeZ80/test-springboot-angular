import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PageResponse, Task, CreateTaskRequest } from '../models/task.model';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl + '/tasks';

  fetchTasks(page = 0, size = 10, sort = 'id,desc', completed?: boolean): Observable<PageResponse<Task>> {
    let url = `${this.apiUrl}?page=${page}&size=${size}&sort=${sort}`;
    if (completed !== undefined) {
      url += `&completed=${completed}`;
    }
    return this.http.get<PageResponse<Task>>(url);
  }

  fetchTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  updateTaskStatus(id: number, completed: boolean) {
    return this.http.patch<Task>(`${this.apiUrl}/${id}/status`, { completed });
  }

  createTask(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, request);
  }

  fetchPendingTasks(page = 0, size = 10, sort = 'id,desc'): Observable<PageResponse<Task>> {
    const url = `${this.apiUrl}/pending?page=${page}&size=${size}&sort=${sort}`;
    return this.http.get<PageResponse<Task>>(url);
  }
} 