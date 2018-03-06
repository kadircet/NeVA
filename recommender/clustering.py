import cacher
import datetime
from recommendation_experiments import utils


def main():
    _cacher = cacher.Cacher()
    now = datetime.datetime.now()
    time = now.hour * 60 + now.minute
    current_context = [time]

    dataset = utils.ExtractFeaturesForAll(
        "recommendation_experiments/dataset.csv")

    for user_id in dataset:
        nearest_elements = utils.GetNearestElements(
            dataset[user_id], current_context, k=10)
        _cacher.UpdateUserCache(user_id, nearest_elements)


if __name__ == "__main__":
    main()
