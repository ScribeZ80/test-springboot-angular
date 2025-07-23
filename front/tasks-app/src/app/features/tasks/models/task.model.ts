export interface Task {
  id: number;
  label: string;
  description: string;
  completed: boolean;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CreateTaskRequest {
  label: string;
  description: string;
} 