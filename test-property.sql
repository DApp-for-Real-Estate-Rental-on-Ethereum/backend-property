-- Test Property for Pricing API Testing
-- This creates a property with all required fields for price prediction

-- First, ensure we have a property type
INSERT INTO property_types (type) VALUES ('Apartment') ON CONFLICT DO NOTHING;
INSERT INTO property_types (type) VALUES ('House') ON CONFLICT DO NOTHING;

-- Get property type ID (assuming Apartment exists)
DO $$
DECLARE
    prop_type_id BIGINT;
    test_user_id VARCHAR := '3';  -- Change this to a real user ID if needed
    test_property_id VARCHAR := 'prop-test-001';
    address_id_val BIGINT;
BEGIN
    -- Get property type ID
    SELECT id INTO prop_type_id FROM property_types WHERE type = 'Apartment' LIMIT 1;
    
    IF prop_type_id IS NULL THEN
        INSERT INTO property_types (type) VALUES ('Apartment') RETURNING id INTO prop_type_id;
    END IF;
    
    -- Create address
    INSERT INTO addresses (address, city, country, latitude, longitude, postal_code)
    VALUES ('123 Test Street', 'Casablanca', 'Morocco', 33.5731, -7.5898, 20000)
    RETURNING id INTO address_id_val;
    
    -- Create property
    INSERT INTO properties (
        id, user_id, status, daily_price, deposit_amount, price,
        negotiation_percentage, discount_five_days, discount_fifteen_days, discount_one_month,
        capacity, number_of_bedrooms, number_of_beds, number_of_bathrooms,
        title, description, address_id, type_id
    ) VALUES (
        test_property_id,
        test_user_id,
        'APPROVED',
        400.0,  -- daily_price
        200.0,  -- deposit_amount
        400.0,  -- price (legacy)
        10.0,   -- negotiation_percentage
        5,      -- discount_five_days (%)
        10,     -- discount_fifteen_days (%)
        15,     -- discount_one_month (%)
        4,      -- capacity
        2,      -- number_of_bedrooms
        3,      -- number_of_beds
        1,      -- number_of_bathrooms
        'Test Property for Pricing API',
        'A test property in Casablanca for testing the pricing prediction API',
        address_id_val,
        prop_type_id
    ) ON CONFLICT (id) DO UPDATE SET
        daily_price = EXCLUDED.daily_price,
        number_of_bedrooms = EXCLUDED.number_of_bedrooms,
        number_of_beds = EXCLUDED.number_of_beds;
    
    -- Add some property images (optional but good for testing)
    INSERT INTO property_images (propety_id, url, is_cover)
    VALUES 
        (test_property_id, 'https://example.com/image1.jpg', true),
        (test_property_id, 'https://example.com/image2.jpg', false),
        (test_property_id, 'https://example.com/image3.jpg', false)
    ON CONFLICT DO NOTHING;
    
    RAISE NOTICE 'Test property created with ID: %', test_property_id;
END $$;

-- Verify the property was created
SELECT 
    p.id,
    p.title,
    a.city,
    p.daily_price,
    p.number_of_bedrooms,
    p.number_of_beds,
    (SELECT COUNT(*) FROM property_images WHERE propety_id = p.id) as image_count
FROM properties p
LEFT JOIN addresses a ON p.address_id = a.id
WHERE p.id = 'prop-test-001';

