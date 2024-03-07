import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IReview } from '@/shared/model/review.model';

import ReviewService from './review.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class Review extends Vue {
  @Inject('reviewService') private reviewService: () => ReviewService;
  private removeId: number = null;

  public reviews: IReview[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllReviews();
  }

  public clear(): void {
    this.retrieveAllReviews();
  }

  public retrieveAllReviews(): void {
    this.isFetching = true;

    this.reviewService()
      .retrieve()
      .then(
        res => {
          this.reviews = res.data;
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

  public prepareRemove(instance: IReview): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeReview(): void {
    this.reviewService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('jdemoApp.review.deleted', { param: this.removeId });
        this.$bvToast.toast(message.toString(), {
          toaster: 'b-toaster-top-center',
          title: 'Info',
          variant: 'danger',
          solid: true,
          autoHideDelay: 5000,
        });
        this.removeId = null;
        this.retrieveAllReviews();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
