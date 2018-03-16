import cacher
import datetime
import utils


# TODO(kadircet): Implement support for event based updating after a user's
# like/dislike/history information arrives.
def main():
    _cacher = cacher.Cacher()
    now = datetime.datetime.now()
    time = now.hour * 60 + now.minute
    current_context = [time]

    # TODO(kadircet): Perform loop in parallel.
    for user_id in utils.GetUserIDs():
        nearest_elements = utils.GetNearestElements(
            user_id, current_context, k=10)
        _cacher.UpdateUserCache(user_id, nearest_elements)


if __name__ == "__main__":
    main()
