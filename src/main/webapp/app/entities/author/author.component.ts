import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAuthor } from '@/shared/model/author.model';

import AuthorService from './author.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class Author extends Vue {
  @Inject('authorService') private authorService: () => AuthorService;
  private removeId: number = null;

  public authors: IAuthor[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAuthors();
  }

  public clear(): void {
    this.retrieveAllAuthors();
  }

  public retrieveAllAuthors(): void {
    this.isFetching = true;

    this.authorService()
      .retrieve()
      .then(
        res => {
          this.authors = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public handleSyncList(): void {
    this.clear();
  }

  public prepareRemove(instance: IAuthor): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAuthor(): void {
    this.authorService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('jdemoApp.author.deleted', { param: this.removeId });
        this.$bvToast.toast(message.toString(), {
          toaster: 'b-toaster-top-center',
          title: 'Info',
          variant: 'danger',
          solid: true,
          autoHideDelay: 5000,
        });
        this.removeId = null;
        this.retrieveAllAuthors();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
