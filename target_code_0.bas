t0 = 10
V0 = t0
t1 = 20
V1 = t1
t2 = V1
t3 = V0
IF t2 > t3 THEN GOSUB L1 ELSE GOSUB L2

L1:
PRINT "Hellyeah"
GOTO L3

L2:
PRINT "No"

L3:
t4 = V0
V2 = t4
t5 = F1(V0, V1, V2)
V3 = t5
PRINT V3
END

FUNCTION F1 (t6, t7, t8)
V0 = t6
V1 = t7
V2 = t8
t10 = V0
t11 = V1
t9 = t10 + t11
V3 = t9
t12 = F3(V3, V0, V1)
V5 = t12
F1 = V5
END FUNCTION
FUNCTION F3 (t13, t14, t15)
V0 = t13
V1 = t14
V2 = t15
t16 = V0
V3 = t16
t17 = V1
V4 = t17
t19 = V3
t20 = V4
t18 = t19 * t20
V5 = t18
F3 = V5
END FUNCTION