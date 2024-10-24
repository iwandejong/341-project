cd "$(dirname "$0")"
./target_code_0 &
osascript -e 'tell application "Terminal" to close (every window whose name contains "target_code_0_start.command")' &
osascript -e 'if (count the windows of application "Terminal") is 0 then tell application "Terminal" to quit' &
exit
