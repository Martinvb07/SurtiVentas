import { Role } from '../../../core/auth/models/role.enum';

export interface UserAccount {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
}

export interface UserCreateRequest {
  email: string;
  fullName: string;
  role: Role;
  password: string;
}

export interface UserUpdateRequest {
  fullName: string;
  role: Role;
  active: boolean;
}
