t0 = 10
V0 = t0
t1 = 20
V1 = t1
t2 = V1
t3 = V0
IF t2 > t3 THEN GOSUB L1 ELSE GOSUB L2

L1:
PRINT "Yes"
GOTO L3

L2:
PRINT "No"

L3:
t4 = V0
V2 = t4
PRINT V2
