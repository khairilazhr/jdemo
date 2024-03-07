export interface IAuthor {
  id?: number;
  name?: string;
  age?: string | null;
  birthDate?: Date | null;
}

export class Author implements IAuthor {
  constructor(public id?: number, public name?: string, public age?: string | null, public birthDate?: Date | null) {}
}
