UPDATE resources
   SET content = LOAD_FILE('C:/xampp/htdocs/chromispos/ChromisPOS/sql/Ticket.Buttons.xml')
 WHERE name = 'Ticket.Buttons';
