import datetime
import heapq
import numpy as np
import MySQLdb

NUM_FEATURES = 2
fields = (
    "suggestee_id",
    "timestamp",
)
db = None
kMaxDistThreshold = 60.  # in minutes
kLIKE = 2
kHistoryCoef = 2


def ParseFeature(feature_value, feature_type):
    """
    Parses given feature_value of type feature_type into a meaningful form.

    Raw versions of features might be too verbose or of wrong type. This parsing
    ensures that all values are of desired types.
    """
    if feature_type == "suggestee_id":
        return int(feature_value)
    elif feature_type == "timestamp":
        feature_value = datetime.datetime.utcfromtimestamp(feature_value)
        hour, minute = feature_value.hour, feature_value.minute
        return hour * 60 + minute
    raise Exception("Unknown feature type:", feature_type)


# TODO(kadircet): Introduce likes/dislikes into cluster.
def ExtractFeatures(for_user_id):
    """
    Extracts history information of user @for_user_id.

    Return value is a matrix containing N-many rows, with each row in the
    format:
    [suggestee_id, feature_1, feature_2, ...]
    """

    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    inputs = np.ndarray([0, NUM_FEATURES])
    sql = "SELECT {} FROM `user_choice_history` WHERE `user_id` = %s".format(
        ",".join(map(lambda x: '`' + x + '`', fields)))
    with db.cursor() as cur:
        cur.execute(sql, (for_user_id, ))
        for raw_features in cur:
            features = np.array(raw_features).astype(np.float)
            for idx, field in enumerate(fields):
                features[idx] = ParseFeature(features[idx], field)
            inputs = np.vstack((inputs, features))
    return inputs


def GetDist(feature_1, feature_2):
    """
    Returns euclidian distance (Frobenius norm) between two feature vectors.
    """
    return np.linalg.norm(feature_1 - feature_2)


def GetNearestElements(user_id, current_context, k=10):
    """
    Returns k nearest neighbours of current_context in user_history.

    user_history is the output of ExtractFeatures.
    current_context is a feature vector with NUM_FEATURES elements.
    """

    user_history = ExtractFeatures(user_id)
    user_interest = GetUserInterest(user_id, current_context)

    neighbours = []
    counts = {}
    for entry in user_history:
        if entry[0] in user_interest and user_interest[entry[0]] < 0:
            continue
        dist = GetDist(entry[1:], current_context)
        if dist > kMaxDistThreshold:
            continue
        if len(counts) < k:
            heapq.heappush(neighbours, (-dist, entry[0]))
            if entry[0] not in counts:
                counts[entry[0]] = 1
            else:
                counts[entry[0]] += 1
        elif dist < -neighbours[0][0]:
            _, smallest = heapq.heappushpop(neighbours, (-dist, entry[0]))
            counts[smallest] -= 1
            if counts[smallest] == 0:
                del counts[smallest]

    neighbours = []
    for suggestee_id, count in user_interest.items():
        history_count = counts.get(suggestee_id, 0)
        counts.pop(suggestee_id, 0)
        neighbours.append((history_count * kHistoryCoef + count, suggestee_id))
    for suggestee_id, history_count in counts.items():
        neighbours.append((history_count * kHistoryCoef, suggestee_id))
    neighbours.sort()

    return tuple(map(lambda x: int(x[1]), neighbours))


def GetUserIDs():
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "SELECT `id` FROM `user`"
    with db.cursor() as cur:
        cur.execute(sql)
        ids = (row[0] for row in cur)
    return ids


def GetUserInterest(user_id, current_context):
    """
    Gets user interests in suggestions related to a context, using feedbacks.

    Returns an interest map from suggestee_ids to interest values.
    """
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "SELECT `suggestee_id`, `timestamp`, `feedback` FROM " + \
            "`user_recommendation_feedback` WHERE `user_id` = %s"
    interest = {}
    with db.cursor() as cur:
        cur.execute(sql, (user_id, ))
        for raw_features in cur:
            features = np.array(raw_features).astype(np.float)
            for idx, field in enumerate(fields):
                features[idx] = ParseFeature(features[idx], field)
            suggestee_id, timestamp, feedback = features
            feedback = 1 if feedback == kLIKE else -1
            if GetDist([timestamp], current_context) > kMaxDistThreshold:
                continue
            if suggestee_id not in interest:
                interest[suggestee_id] = feedback
            else:
                interest[suggestee_id] += feedback

    return interest
