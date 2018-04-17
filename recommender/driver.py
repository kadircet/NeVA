import sys
import os
import datetime
import logging
import traceback
import time
kWaitInteval = 1.  # 1 second.


def main():
    import clustering

    while True:
        try:
            clustering.main()
        except Exception as e:
            logging.log(logging.WARN, e, exc_info=1)
        time.sleep(kWaitInteval)


if __name__ == "__main__":
    now = str(datetime.datetime.now()).replace(" ", "_")
    log_file_name = os.path.join(
        os.path.dirname(sys.argv[0]),
        'logs/' + now + "_" + str(os.getpid()) + '.log')
    logging.basicConfig(
        filename=log_file_name,
        level=logging.INFO,
        format='%(asctime)s %(levelname)s %(name)s %(message)s')
    main()
