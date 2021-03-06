SELECT *
	FROM EMP e ;
	
SELECT EMP_ID, EMP_NAME, e.RANK_NO, RANK_NAME
	FROM CHAT_ROOM,
		JSON_TABLE(ROOM_MEM, '$.ROOM[*]'
			COLUMNS (
			 	IDS VARCHAR(20) PATH '$.id',
			 	JDATE VARCHAR(30) PATH '$.join'
			)
		) AS jt,
		(SELECT EMP_ID,EMP_NAME, RANK_NO 
          	FROM EMP
         ) e,
         (SELECT RANK_NAME,RANK_NO
         	FROM "RANK"
         ) r
	WHERE ROOM_ID = '5'
	AND jt.IDS = e.EMP_ID
	AND e.RANK_NO = r.RANK_NO
	ORDER BY 3 DESC, TO_DATE(jt.JDATE,'YYYYMMDDHH24MISS') DESC;