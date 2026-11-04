export interface Category { categoryId: string; name: string; description: string; vatRate: number; }
export type CreateCategory = Omit<Category, 'categoryId'>;
