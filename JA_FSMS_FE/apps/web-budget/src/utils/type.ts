export interface OperationItem {
  key: string;
  text?: string;
  icon: string;
  access?: string;
  class?: string;
  type?: 'dashed' | 'default' | 'ghost' | 'link' | 'primary' | 'text';
  props?: Record<string, any>;
  loading?: number;
}
