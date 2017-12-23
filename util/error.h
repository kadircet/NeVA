#ifndef _NEVA_UTIL_ERROR_H_
#define _NEVA_UTIL_ERROR_H_

#define RETURN_IF_ERROR(expr)                                                \
  do {                                                                       \
    /* Using _status below to avoid capture problems if expr is "status". */ \
    const ::grpc::Status _status = (expr);                                   \
    if (GOOGLE_PREDICT_FALSE(!_status.ok())) return _status;                 \
  } while (0)

#endif
