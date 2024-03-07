export interface IBook {
  id?: number;
  title?: string;
  description?: string | null;
  publicationDate?: Date | null;
  price?: number | null;
  coverContentType?: string | null;
  cover?: string | null;
}

export class Book implements IBook {
  constructor(
    public id?: number,
    public title?: string,
    public description?: string | null,
    public publicationDate?: Date | null,
    public price?: number | null,
    public coverContentType?: string | null,
    public cover?: string | null
  ) {}
}
