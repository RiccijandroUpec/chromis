SET @c = '<?xml version="1.0" encoding="UTF-8"?>
<!--
    Chromis POS  - Open Source Point of Sale
    This file is part of Chromis POS Version Chromis V1.5.4
    Copyright 2015-2022

    http://www.chromis.co.uk
 -->

<!-- To ENABLE a feature remove the beginning and of line commentors -->
<!-- To DISABLE a feature you must add the beginning and of line commentors -->

<configuration>
    <!-- Sales Ticket Button Bar - Show icons **************************************** -->
    <!-- SET line note (Default=DISABLED) -->
    <button key="button.notes" image="addnote.png" code="script.AddLineNote"/>

    <!-- SET Line or Total Discount (Default=TotalDiscount= ENABLED) -->
    <!-- <button key="button.totaldiscount" image="ticketdiscount.png" code="script.totaldiscount"/> -->
    <button key="button.linediscount" image="linediscount.png" code="script.linediscount"/>

    <!-- SET Waiter''s name (Default=DISABLED) -->
    <!-- <button key="button.setperson" image="user.png" code="script.SetPerson"/>  -->

    <!-- SET Ticket Print Preview (Default=ENABLED) -->
    <button key="button.print" image="printer24.png" template="Printer.TicketPreview"/>

    <!-- SET Open Cashdrawer (Default=ENABLED) -->
    <button key="button.opendrawer" image="cashdrawer.png" template="Printer.OpenDrawer"/>



    <!-- END Sales Ticket Button Bar - Show icons -->


    <!-- SET Ticket Button Bar - Text Only ****************************************************************** -->
    <!-- <button key="button.refundit" name="button.refundit" code="script.Refundit"/> -->
    <!-- <button key="button.linediscount" name="button.linediscount" code="script.linediscount"/> -->
    <!-- <button key="button.discount" name="button.totaldiscount" code="script.totaldiscount"/> -->
    <!-- <button key="button.setperson" name="button.setperson" code="script.SetPerson"/> -->
    <!-- <button key="button.sendorder" name="button.sendorder" code="script.SendOrder"/> -->
    <!-- <button key="button.print" titlekey="button.print" template="Printer.TicketPreview"/> -->
    <!-- <button key="button.opendrawer" titlekey="button.opendrawer" template="Printer.OpenDrawer"/> -->
    <!-- END Ticket Button Bar - Text Only -->

    <!-- SET Product Area Size ********************************************* -->
    <!-- Maximum Category height for 800x480 for PDA in Full Screen mode -->
    <!-- <cat-height value="100" /> -->

    <!-- Maximum Category height for 800x600 -->
    <!--  <cat-height value="155" /> -->

    <!-- Maximum Category height for 1024x768 in Full Screen mode (Default=ENABLED) -->
    <cat-height value="320" />
    <!-- <cat-width value="600" /> -->

    <!-- Maximum Category height for 1280x1024 in Full Screen mode -->
    <!--  <cat-height value="640" /> -->
    <!-- END Product Area Size -->


    <!-- ADDITIONAL FEATURES ************************************************* -->
    <!-- SET Show Change in Cash Sale (Default=ENABLED -->
    <event key="ticket.close" code="Ticket.Close"/>

    <!-- SET Consolidate Receipt - Multiple ProductID's (Default=DISABLED) -->
    <!-- <event  key="ticket.total" code="script.ReceiptConsolidate"/> -->

    <!-- <event key="ticket.change" code="event.change"/> -->
    <!-- <event key="ticket.addline" code="event.addline"/> -->
    <!-- <event key="ticket.removeline" code="event.removeline"/> -->
    <!-- <event key="ticket.setline" code="event.setline"/> -->
    <!-- <event key="ticket.show" code="event.show"/> -->
    <!-- <event key="ticket.total" code="script.Event.Total"/> -->
    <!-- <event key="ticket.total" code="event.total"/> -->
    <!-- <event key="ticket.close" code="event.close"/> -->
    <!-- <event key="ticket.change" code="script.ServiceCharge" /> -->


    <!-- Custom scan code processor script  -->
    <!-- <event key="script.CustomCodeProcessor" code="script.CustomCodeProcessor"/> -->


    <!-- <pricevisible value="true" /> -->

</configuration>';

UPDATE resources SET content = @c WHERE name = 'Ticket.Buttons';
