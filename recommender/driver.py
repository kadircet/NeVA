import sys
import traceback
import time
kWaitInteval = 10 * 60  # 10 mins.


def main():
    import clustering

    while True:
        try:
            clustering.main()
        except Exception as e:
            print(e)
        time.sleep(kWaitInteval)


if __name__ == "__main__":
    main()
