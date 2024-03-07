import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IBook } from '@/shared/model/book.model';
import BookService from './book.service';

@Component
export default class BookDetails extends mixins(JhiDataUtils) {
  @Inject('bookService') private bookService: () => BookService;
  public book: IBook = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.bookId) {
        vm.retrieveBook(to.params.bookId);
      }
    });
  }

  public retrieveBook(bookId) {
    this.bookService()
      .find(bookId)
      .then(res => {
        this.book = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
