import { Role } from './role.enum';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
}
