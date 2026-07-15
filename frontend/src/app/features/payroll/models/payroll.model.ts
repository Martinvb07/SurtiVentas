export interface Employee {
  id: number;
  fullName: string;
  position: string;
  salary: number;
  active: boolean;
}

export interface PayrollPayment {
  id: number;
  employeeId: number;
  employeeName: string;
  amount: number;
  period: string;
  note: string | null;
  registeredByName: string;
  paidAt: string;
}

export interface EmployeeRequest {
  fullName: string;
  position: string;
  salary: number;
}

export interface EmployeeUpdateRequest extends EmployeeRequest {
  active: boolean;
}

export interface PayrollPaymentRequest {
  amount: number;
  period: string;
  note: string | null;
}
