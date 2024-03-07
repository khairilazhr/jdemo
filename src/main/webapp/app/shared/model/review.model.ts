export interface IReview {
  id?: number;
  title?: string | null;
  content?: string | null;
  rating?: number | null;
}

export class Review implements IReview {
  constructor(public id?: number, public title?: string | null, public content?: string | null, public rating?: number | null) {}
}
