-- ======================================================
-- FoodShare - Database Verification Script
-- ======================================================

USE foodshare_db;

-- 1. View All Surplus Food Listings
SELECT 
    id, 
    food_name, 
    food_type, 
    quantity, 
    unit, 
    safe_to_eat_until, 
    status 
FROM food_listings;

-- 2. View All Claims and Pickups
SELECT 
    c.id AS claim_id,
    fl.food_name,
    fl.quantity,
    fl.unit,
    n.name AS ngo_name,
    c.claimed_at,
    c.status,
    c.collected_at
FROM claims c
JOIN food_listings fl ON c.food_listing_id = fl.id
JOIN ngos n ON c.ngo_id = n.id;

-- 3. View Donors
SELECT id, name, email, phone, address FROM donors;

-- 4. View NGOs
SELECT id, name, contact_person, email, phone, address FROM ngos;
