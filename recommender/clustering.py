import cacher
import datetime
import utils
import numpy as np

kWholeUpdatePeriod = 10 * 60  # 10 mins.


def UpdateUsers(users, suggestees, _cacher, current_context):
    for user_id in users:
        nearest_elements = utils.GetNearestElements(
            user_id, current_context, suggestees, k=10)
        _cacher.UpdateUserCache(user_id, nearest_elements)
        utils.MarkUserAsProcessed(user_id)


# TODO(kadircet): Implement support for event based updating after a user's
# like/dislike/history information arrives.
def main():
    now = datetime.datetime.now()
    time = now.hour * 60 + now.minute
    current_context = np.array([time]).astype(np.float)

    if not hasattr(main, "suggestees"):
        main.suggestees = utils.GetSuggesteeIDs()
        main._cacher = cacher.Cacher()
        main.last_called = now

    users = []
    if now - main.last_called > kWholeUpdatePeriod:
        users = utils.GetUserIDs()
    else:
        users = utils.GetUpdatedUserIDs()
    UpdateUsers(users, main.suggestees, main._cacher, current_context)


if __name__ == "__main__":
    main()
