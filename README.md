# Graylog S3 Input
An AWS Lambda function that reads log messages from AWS S3 and sends them to a Graylog GELF (TCP) input.

Detailed documentation coming soon.

## Architectural Overview

```
                      _____                 _                    __          _______     _____ ____     _____                   _
                     / ____|               | |                  /\ \        / / ____|   / ____|___ \   |_   _|                 | |
                    | |  __ _ __ __ _ _   _| | ___   __ _      /  \ \  /\  / / (___    | (___   __) |    | |  _ __  _ __  _   _| |_
                    | | |_ | '__/ _` | | | | |/ _ \ / _` |    / /\ \ \/  \/ / \___ \    \___ \ |__ <     | | | '_ \| '_ \| | | | __|
                    | |__| | | | (_| | |_| | | (_) | (_| |   / ____ \  /\  /  ____) |   ____) |___) |   _| |_| | | | |_) | |_| | |_
                     \_____|_|  \__,_|\__, |_|\___/ \__, |  /_/    \_\/  \/  |_____/   |_____/|____/   |_____|_| |_| .__/ \__,_|\__|
                                       __/ |         __/ |                                                         | |
                                      |___/         |___/                                                          |_|








   New files written                        Lambda function is notified when files are written.               GELF TCP Input running on Graylog node or cluster
   to S3.                                   Function reads file, parses it, and sends it to
                                            Graylog TCP input
    _____ ____                              _                     _         _                                   _____                 _
   / ____|___ \                .           | |                   | |       | |                     .           / ____|               | |
  | (___   __) |      .........;;.         | |     __ _ _ __ ___ | |__   __| | __ _       .........;;.        | |  __ _ __ __ _ _   _| | ___   __ _
   \___ \ |__ <       :::::::::;;;;.       | |    / _` | '_ ` _ \| '_ \ / _` |/ _` |      :::::::::;;;;.      | | |_ | '__/ _` | | | | |/ _ \ / _` |
   ____) |___) |      :::::::::;;:'        | |___| (_| | | | | | | |_) | (_| | (_| |      :::::::::;;:'       | |__| | | | (_| | |_| | | (_) | (_| |
  |_____/|____/                :'          |______\__,_|_| |_| |_|_.__/ \__,_|\__,_|               :'          \_____|_|  \__,_|\__, |_|\___/ \__, |
                                                                                                                                 __/ |         __/ |
                      Notifies Lambda                                                     Sent as GELF                          |___/         |___/
                      automatically                                                       over TCP
                      with new files



Arrow credit: http://ascii.co.uk/art/arrow
```