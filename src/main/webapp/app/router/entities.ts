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
// prettier-ignore
const Review = () => import('@/entities/review/review.vue');
// prettier-ignore
const ReviewUpdate = () => import('@/entities/review/review-update.vue');
// prettier-ignore
const ReviewDetails = () => import('@/entities/review/review-details.vue');
// prettier-ignore
const Order = () => import('@/entities/order/order.vue');
// prettier-ignore
const OrderUpdate = () => import('@/entities/order/order-update.vue');
// prettier-ignore
const OrderDetails = () => import('@/entities/order/order-details.vue');
// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default [
  {
    path: '/author',
    name: 'Author',
    component: Author,
    meta: { authorities: [Authority.ADMIN] },
  },
  {
    path: '/author/new',
    name: 'AuthorCreate',
    component: AuthorUpdate,
    meta: { authorities: [Authority.ADMIN] },
  },
  {
    path: '/author/:authorId/edit',
    name: 'AuthorEdit',
    component: AuthorUpdate,
    meta: { authorities: [Authority.ADMIN] },
  },
  {
    path: '/author/:authorId/view',
    name: 'AuthorView',
    component: AuthorDetails,
    meta: { authorities: [Authority.ADMIN] },
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
  {
    path: '/review',
    name: 'Review',
    component: Review,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/review/new',
    name: 'ReviewCreate',
    component: ReviewUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/review/:reviewId/edit',
    name: 'ReviewEdit',
    component: ReviewUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/review/:reviewId/view',
    name: 'ReviewView',
    component: ReviewDetails,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/order',
    name: 'Order',
    component: Order,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/order/new',
    name: 'OrderCreate',
    component: OrderUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/order/:orderId/edit',
    name: 'OrderEdit',
    component: OrderUpdate,
    meta: { authorities: [Authority.USER] },
  },
  {
    path: '/order/:orderId/view',
    name: 'OrderView',
    component: OrderDetails,
    meta: { authorities: [Authority.USER] },
  },
  // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
];
