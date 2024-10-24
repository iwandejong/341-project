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
F1 V0, V1, V2
END

SUB F1 (t5, t6, t7)
V0 = t5
V1 = t6
V2 = t7
t8 = V0
V3 = t8
t9 = V1
V4 = t9
t11 = V3
t12 = V4
t10 = t11 + t12
V5 = t10
PRINT V3
PRINT V4
PRINT V5
END SUB