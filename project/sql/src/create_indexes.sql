DROP INDEX IF EXISTS user_ID;

CREATE INDEX user_ID
ON Users
(userID);

DROP INDEX IF EXISTS Booking_customer;

CREATE INDEX Booking_customer
ON RoomBookings
(customerID);

DROP INDEX IF EXISTS Rooms_ID;

CREATE INDEX Rooms_ID
ON Rooms
(hotelID);