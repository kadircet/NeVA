import datetime
import heapq
import numpy as np
import MySQLdb
from random import shuffle
from functools import reduce

NUM_FEATURES = 2
fields = (
    "suggestee_id",
    "timestamp",
)
db = None
kMaxDistThreshold = 60.  # in minutes
kLIKE = 2
kHistoryCoef = 2
kColdStartThreshold = 30
kMinSimilarityThreshold = 0.5


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


def GetTagWeights(suggestees):
    tag_counts = {}
    for suggestee in suggestees:
        tags = GetTagsForSuggestee(suggestee)
        for tag in tags:
            if tag not in tag_counts:
                tag_counts[tag] = 1
            else:
                tag_counts[tag] += 1
    return tuple((k, v) for k, v in tag_counts.items())


def GetNearestElements(user_id, current_context, suggestees, k=10):
    """
    Returns k nearest neighbours of current_context in user_history.

    user_history is the output of ExtractFeatures.
    current_context is a feature vector with NUM_FEATURES elements.
    """

    if type(user_id) is int:
        user_history = ExtractFeatures(user_id)
    else:
        user_history = user_id
    user_interest = GetUserInterest(user_id, current_context, suggestees)

    neighbours = []
    counts = {}
    for entry in user_history:
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
            if entry[0] not in counts:
                counts[entry[0]] = 1
            else:
                counts[entry[0]] += 1
            counts[smallest] -= 1
            if counts[smallest] == 0:
                del counts[smallest]

    # TODO(kadircet): Add data coming from cold start or maybe most liked N
    # elements into the base tags too.
    base_tags = GetTagWeights(counts.keys())
    similar_suggestees = GetSimilarSuggestees(
        None, base_tags=base_tags, similarity_metric=WeightedJaccardSimilarity)
    neighbours = []
    for suggestee_id, count in user_interest.items():
        history_count = counts.get(suggestee_id, 0)
        # If user simply disliked and never eaten it, abandon the choice.
        if history_count == 0 and count < 0:
            continue
        counts.pop(suggestee_id, 0)
        neighbours.append((history_count * kHistoryCoef + count, suggestee_id))
    for suggestee_id, history_count in counts.items():
        neighbours.append((history_count * kHistoryCoef, suggestee_id))
    max_count = max(max(neighbours)[0], 1)

    def CountsToProb(x):
        return (x[0] / max_count, x[1])

    neighbours = list(map(CountsToProb, neighbours))
    neighbours.extend(similar_suggestees)
    neighbours.sort()
    neighbours.reverse()

    return tuple(map(lambda x: int(x[1]), neighbours))[:20]


def GetUserIDs():
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "SELECT `id` FROM `user`"
    with db.cursor() as cur:
        cur.execute(sql)
        ids = (row[0] for row in cur)
    return ids


def GetUserInterest(user_id, current_context, suggestees):
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

    if len(interest) < kColdStartThreshold:
        suggestees_shuffled = list(suggestees)
        shuffle(suggestees_shuffled)
        delta = kColdStartThreshold - len(interest) + 5
        if delta > len(suggestees_shuffled):
            delta = len(suggestees_shuffled)
        for suggestee_id in suggestees_shuffled[:delta]:
            if suggestee_id in interest:
                continue
            interest[suggestee_id] = 0
            if len(interest) >= kColdStartThreshold:
                break

    return interest


def GetSuggesteeIDs(category=1):
    """
    Gets all ids for all suggestees in the database.
    """
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "SELECT `id` FROM `suggestee`"
    suggestee_ids = []
    with db.cursor() as cur:
        cur.execute(sql)
        for (suggestee_id, ) in cur:
            if len(GetTagsForSuggestee(suggestee_id)) == 0:
                continue
            suggestee_ids.append(suggestee_id)
    return tuple(suggestee_ids)


def WeightedJaccardSimilarity(set1, set2):
    """
    Calculates Jaccard similarity between two sets with one of them containing 
    weighted elements.

    set1 is the set containing weigths as well, it is a tuple of pairs, 
        (element, weight).
    set2 is just a set.
    """
    if len(set1) + len(set2) == 0:
        return 1.
    elems_in_first = {k: v for (k, v) in set1}

    def ReduceToUnion(x, y):
        if y in elems_in_first:
            x += elems_in_first[y]
        else:
            x += 1
        return x

    total_size = reduce(lambda x, y: x + y[1], set1, 0)
    total_size = reduce(ReduceToUnion, set2, total_size)

    def ReduceToIntersection(x, y):
        """
        x is current intersection size.
        y is the next element in set2.
        """
        if y in elems_in_first:
            x += elems_in_first[y]
        return x

    size_of_intersection = reduce(ReduceToIntersection, set2, 0)
    return size_of_intersection / (total_size - size_of_intersection)


def GetSuggesteeSimilarity(suggestee1_tags, suggestee2_tags):
    """
    Calculates Jaccard similarity between two sets.
    """
    total_size = len(suggestee1_tags) + len(suggestee2_tags)
    if total_size == 0:
        return 1.
    size_of_intersection = 0
    elems_in_first = {}
    for tag in suggestee1_tags:
        elems_in_first[tag] = True
    for tag in suggestee2_tags:
        if tag in elems_in_first:
            size_of_intersection += 1
    return size_of_intersection / (total_size - size_of_intersection)


def GetTagsForSuggestee(suggestee_id):
    """
    Returns tags for a suggestee as a tuple.
    """
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "SELECT `tag_id` FROM `suggestee_tags` WHERE `suggestee_id`=%s"
    with db.cursor() as cur:
        cur.execute(sql, (suggestee_id, ))
        tag_ids = (x[0] for x in cur.fetchall())
    return tuple(tag_ids)


def GetSimilarSuggestees(suggestee_id,
                         k=5,
                         base_tags=None,
                         similarity_metric=GetSuggesteeSimilarity):
    """
    Returns similar suggestion items to suggestee_id as a tuple of pairs,
    (similarity, suggestee_id).
    """
    if base_tags == None:
        base_tags = GetTagsForSuggestee(suggestee_id)
    suggestee_ids = GetSuggesteeIDs()
    similar_suggestees = []
    for id in suggestee_ids:
        if id == suggestee_id:
            continue
        item_tags = GetTagsForSuggestee(id)
        similarity = similarity_metric(base_tags, item_tags)
        if similarity < kMinSimilarityThreshold:
            continue
        if len(similar_suggestees) < k:
            heapq.heappush(similar_suggestees, (similarity, id))
        elif similarity > similar_suggestees[0][0]:
            heapq.heappushpop(similar_suggestees, (similarity, id))

    similar_suggestees.sort()
    similar_suggestees.reverse()
    return tuple(similar_suggestees)


def MeasureAccuracy(user_id, k=10):
    """
    Measures user accuracy over time by introducing more of user's history and 
    likes into the pipeline.

    Returns an array of tuples, first one containing maximum similarity between
    user's choice and what we've recommended. Second, the order of user's choice
    in suggestion list, if exists, -1 otherwise.
    """
    suggestees = GetSuggesteeIDs()
    user_features = ExtractFeatures(user_id)
    results = []

    for idx in range(len(user_features)):
        suggestee_id, current_context = user_features[idx]
        nearest_elements = GetNearestElements(
            user_features[:idx], current_context, suggestees, k=k)
        suggestee_tags = GetTagsForSuggestee(suggestee_id)
        found_idx = -1
        avg_similarity = .0
        for idx, suggestion_id in enumerate(nearest_elements):
            if suggestee_id == suggestion_id:
                found_idx = idx
            suggestion_tags = GetTagsForSuggestee(suggestion_id)
            if idx < k:
                avg_similarity += GetSuggesteeSimilarity(
                    suggestee_tags, suggestion_tags)
        avg_similarity /= k
        results.append((found_idx, avg_similarity))
    return results


def GetUpdatedUserIDs():
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "SELECT `user_id` FROM `user_needs_update` WHERE `needs_update` = 0"
    with db.cursor() as cur:
        cur.execute(sql)
        ids = (row[0] for row in cur)
    return ids


def MarkUserAsProcessed(user_id):
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")
    sql = "UPDATE `user_needs_update` SET `needs_update` = 0 WHERE `user_id` = %s"
    with db.cursor() as cur:
        cur.execute(sql, (user_id, ))
    db.commit()


def GetMostDiverseItem(current_tags, item_list, items_in_list):
    result = None
    min_similarity = None
    result_tags = None
    for item_id in item_list:
        if item_id in items_in_list:
            continue
        item_tags = GetTagsForSuggestee(item_id)
        current_similarity = WeightedJaccardSimilarity(current_tags, item_tags)
        if min_similarity == None or min_similarity > current_similarity:
            min_similarity = current_similarity
            result = item_id
            result_tags = item_tags
    return result, result_tags


def GetMostDiverseItems(k=30):
    diverse_items = []
    diverse_tags = []
    position_in_tag_list = {}
    items_in_list = {}

    suggestees = GetSuggesteeIDs()
    while len(diverse_items) < k:
        item_id, item_tags = GetMostDiverseItem(diverse_tags, suggestees,
                                                items_in_list)
        items_in_list[item_id] = True
        diverse_items.append(item_id)
        for tag_id in item_tags:
            tag_position = position_in_tag_list.get(tag_id, -1)
            if tag_position == -1:
                position_in_tag_list[tag_id] = len(diverse_tags)
                diverse_tags.append([tag_id, 1])
            else:
                diverse_tags[tag_position][1] += 1

    return diverse_items


def UpdateMostDiverseItems():
    global db
    if db == None:
        db = MySQLdb.connect("localhost", "neva", "", "neva")

    delete_query = "DELETE FROM `diverse_item_cache`"
    insert_query = "INSERT INTO `diverse_item_cache` (`suggestee_id`) VALUES (%s)"

    most_diverse_items = GetMostDiverseItems()
    with db.cursor() as cur:
        cur.execute(delete_query)
        for item_id in most_diverse_items:
            cur.execute(insert_query, (item_id, ))
    db.commit()
