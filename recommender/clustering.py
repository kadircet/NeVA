import cacher
import datetime
import utils


def main():
    _cacher = cacher.Cacher()
    now = datetime.datetime.now()
    time = now.hour * 60 + now.minute
    current_context = [time]

    for user_id in utils.GetUserIDs():
        nearest_elements = utils.GetNearestElements(
            user_id, current_context, k=10)
        _cacher.UpdateUserCache(user_id, nearest_elements)


if __name__ == "__main__":
    main()
