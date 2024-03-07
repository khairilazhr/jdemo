export interface IOrder {
  id?: number;
  name?: string | null;
  totalprice?: number | null;
}

export class Order implements IOrder {
  constructor(public id?: number, public name?: string | null, public totalprice?: number | null) {}
}
