export interface Product {
  productId: string;
  name: string;
  price: number;
  categoryId: string | null;
  category: string;
  description: string;
  status: 'ACTIVE' | 'INACTIVE';
}
