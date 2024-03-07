import { Component, Vue, Inject } from 'vue-property-decorator';

import { IReview } from '@/shared/model/review.model';
import ReviewService from './review.service';

@Component
export default class ReviewDetails extends Vue {
  @Inject('reviewService') private reviewService: () => ReviewService;
  public review: IReview = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.reviewId) {
        vm.retrieveReview(to.params.reviewId);
      }
    });
  }

  public retrieveReview(reviewId) {
    this.reviewService()
      .find(reviewId)
      .then(res => {
        this.review = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
