import { Authority } from '@/shared/security/authority';
/* tslint:disable */
// prettier-ignore

// prettier-ignore
const Author = () => import('@/entities/author/author.vue');
// prettier-ignore
const AuthorUpdate = () => import('@/entities/author/author-update.vue');
// prettier-ignore
const AuthorDetails = () => import('@/entities/author/author-details.vue');
// prettier-ignore
const Book = () => import('@/entities/book/book.vue');
// prettier-ignore
const BookUpdate = () => import('@/entities/book/book-update.vue');
// prettier-ignore
const BookDetails = () => import('@/entities/book/book-details.vue');
// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default [
  {
    path: '/author',
    name: 'Author',
    component: Author,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/author/new',
    name: 'AuthorCreate',
    component: AuthorUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/author/:authorId/edit',
    name: 'AuthorEdit',
    component: AuthorUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/author/:authorId/view',
    name: 'AuthorView',
    component: AuthorDetails,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/book',
    name: 'Book',
    component: Book,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/book/new',
    name: 'BookCreate',
    component: BookUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/book/:bookId/edit',
    name: 'BookEdit',
    component: BookUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/book/:bookId/view',
    name: 'BookView',
    component: BookDetails,
    meta: { authorities: [Authority.USER] },
  },
  // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
];
