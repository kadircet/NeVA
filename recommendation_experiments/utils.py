import datetime
import heapq
import numpy as np

NUM_FEATURES = 2
fields = {
    "user_id": 0,
    "suggestee_id": 1,
    "timestamp": 2,
    "latitude": 3,
    "longitude": 4,
}


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


def ExtractFeatures(for_user_id, dataset_file="dataset.csv"):
    """
    Extracts history information of user @for_user_id.

    Return value is a matrix containing N-many rows, with each row in the
    format:
    [suggestee_id, feature_1, feature_2, ...]
    """

    field_ids = [0] * len(fields)

    inputs = np.ndarray([0, NUM_FEATURES])
    with open(dataset_file, "r") as raw_data_file:
        headers = raw_data_file.readline().split(',')
        for idx, header in enumerate(headers):
            if header in fields:
                field_ids[fields[header]] = idx

        for raw_line in raw_data_file:
            line = np.array(raw_line.split(','))
            line = line[field_ids].astype(np.float)
            user_id = int(line[0])
            if for_user_id != user_id:
                continue
            features = line[1:1 + NUM_FEATURES]
            for field in fields:
                idx = fields[field] - 1
                if idx < NUM_FEATURES and idx >= 0:
                    features[idx] = ParseFeature(features[idx], field)
            inputs = np.vstack((inputs, features))
    return inputs


def GetDist(feature_1, feature_2):
    """
    Returns euclidian distance (Frobenius norm) between two feature vectors.
    """
    return np.linalg.norm(feature_1 - feature_2)


def GetNearestElements(user_history, current_context, k=10):
    """
    Returns k nearest neighbours of current_context in user_history.

    user_history is the output of ExtractFeatures.
    current_context is a feature vector with NUM_FEATURES elements.
    """
    neighbours = []
    for entry in user_history:
        dist = GetDist(entry[1:], current_context)
        if len(neighbours) < k:
            heapq.heappush(neighbours, (-dist, entry[0]))
        elif dist < -neighbours[0][0]:
            heapq.heappushpop(neighbours, (-dist, entry[0]))
    return tuple(map(lambda x: int(x[1]), neighbours))


def GetRecommendation(user_history, possible_elements):
    """
    Picks an element from candidate set considering user_history.
    """
    #TODO(kadircet): Consider user_history as well.
    #TODO(kadircet): Introduce diversity to possible elements after implementing
    #food similarity.
    return np.random.choice(possible_elements)


def GetSuggesteeMapping(dataset_file="dataset.csv"):
    fields = {"suggestee_id": 0, "name": 1}
    field_ids = [0] * len(fields)

    mapping = dict()
    with open(dataset_file, "r") as raw_data_file:
        headers = raw_data_file.readline().split(',')
        for idx, header in enumerate(headers):
            if header in fields:
                field_ids[fields[header]] = idx

        for raw_line in raw_data_file:
            line = np.array(raw_line.split(','))
            line = line[field_ids]
            suggestee_id = int(line[0])
            mapping[suggestee_id] = line[1]
    return mapping
