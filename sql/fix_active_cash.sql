-- Fix active cash/siteguid for terminal Riccijandro
SET @terminal_name := 'Riccijandro';
SET @siteguid := (SELECT guid FROM siteguid LIMIT 1);

-- pick current active_cash for this terminal (or generate a new one)
SET @active_cash := (
    SELECT active_cash
    FROM terminals
    WHERE terminal_name = @terminal_name
    ORDER BY active_cash IS NULL, active_cash DESC
    LIMIT 1
);
SET @active_cash := IFNULL(@active_cash, UUID());

-- next sequence for this host name
SET @nextseq := (SELECT COALESCE(MAX(hostsequence), 0) + 1 FROM closedcash WHERE host = @terminal_name);

-- create the closedcash row if missing
INSERT INTO closedcash (money, host, hostsequence, datestart, dateend, nosales, siteguid)
SELECT @active_cash, @terminal_name, @nextseq, NOW(), NULL, 0, @siteguid
WHERE NOT EXISTS (SELECT 1 FROM closedcash WHERE money = @active_cash);

-- ensure the closedcash row has the right siteguid/hostsequence/host
UPDATE closedcash
   SET siteguid = @siteguid,
       host = @terminal_name,
       hostsequence = COALESCE(hostsequence, @nextseq)
 WHERE money = @active_cash;

-- point the terminal to this cash session and align siteguid
UPDATE terminals
   SET active_cash = @active_cash,
       siteguid = @siteguid
 WHERE terminal_name = @terminal_name;

-- report the final state
SELECT 'siteguid' AS label, @siteguid AS value;
SELECT terminal_key, terminal_name, active_cash, siteguid FROM terminals WHERE terminal_name = @terminal_name;
SELECT money, host, hostsequence, siteguid, datestart, dateend FROM closedcash WHERE money = @active_cash;
