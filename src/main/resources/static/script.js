const BASE_URL = "http://localhost:8080";

// ---------- Tab switching ----------
document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));
    btn.classList.add("active");
    const target = document.getElementById(btn.dataset.tab);
    if (target) target.classList.add("active");

    if (btn.dataset.tab === "listings") loadListings();
    if (btn.dataset.tab === "donors") loadDonors();
    if (btn.dataset.tab === "ngos") loadNGOs();
    if (btn.dataset.tab === "claims") loadClaims();
    if (btn.dataset.tab === "analytics") loadAnalytics();
  });
});

// Helper for alert banners
function showAlert(elementId, message, isError = false) {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.className = `alert-box ${isError ? "error" : "success"}`;
  el.textContent = message;
  setTimeout(() => { el.className = "alert-box"; }, 4000);
}

// Set default Safe-To-Eat time to 3 hours from now
function resetSafeTimeInput() {
  const dt = new Date();
  dt.setHours(dt.getHours() + 3);
  const iso = dt.toISOString().slice(0, 16);
  const input = document.getElementById("safeToEatUntil");
  if (input) input.value = iso;
}

// ================= 1. SURPLUS FOOD LISTINGS =================
const listingForm = document.getElementById("listingForm");
const listingsTableBody = document.querySelector("#listingsTable tbody");

async function loadListings() {
  try {
    const res = await fetch(`${BASE_URL}/api/listings`);
    const listings = await res.json();
    listingsTableBody.innerHTML = "";

    // Also populate Claim dropdown with AVAILABLE listings
    const claimSelect = document.getElementById("claimListingId");
    if (claimSelect) {
      claimSelect.innerHTML = '<option value="">-- Select Available Food Listing --</option>';
    }

    listings.forEach(l => {
      const isAvailable = l.status === "AVAILABLE";
      if (isAvailable && claimSelect) {
        const opt = document.createElement("option");
        opt.value = l.id;
        opt.textContent = `#${l.id} - ${l.foodName} (${l.quantity} ${l.unit})`;
        claimSelect.appendChild(opt);
      }

      const row = document.createElement("tr");
      const safeTime = new Date(l.safeToEatUntil).toLocaleString([], { dateStyle: "short", timeStyle: "short" });
      const badgeClass = `status-${(l.status || "").toLowerCase()}`;

      row.innerHTML = `
        <td>${l.id}</td>
        <td><strong>${l.foodName}</strong></td>
        <td>${l.foodType}</td>
        <td>${l.quantity}</td>
        <td>${l.unit}</td>
        <td>${l.donorName}</td>
        <td>${safeTime}</td>
        <td><span class="status-badge ${badgeClass}">${l.status}</span></td>
        <td>
          <button class="action edit-btn" onclick="editListing(${l.id})">Edit</button>
          <button class="action delete-btn" onclick="deleteListing(${l.id})">Delete</button>
          ${isAvailable ? `<button class="action claim-btn" onclick="quickClaim(${l.id})">Claim</button>` : ""}
        </td>`;
      listingsTableBody.appendChild(row);
    });
    window._listingsCache = listings;
  } catch (err) {
    console.error("Error loading listings", err);
  }
}

listingForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("listingId").value;
  const safeVal = document.getElementById("safeToEatUntil").value;

  const payload = {
    donorId: parseInt(document.getElementById("listingDonorId").value, 10),
    foodName: document.getElementById("foodName").value,
    foodType: document.getElementById("foodType").value,
    quantity: parseFloat(document.getElementById("quantity").value),
    unit: document.getElementById("unit").value,
    safeToEatUntil: safeVal ? (safeVal.length === 16 ? safeVal + ":00" : safeVal) : null
  };

  try {
    let res;
    if (id) {
      res = await fetch(`${BASE_URL}/api/listings/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } else {
      res = await fetch(`${BASE_URL}/api/listings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    }

    if (res.ok) {
      showAlert("listingAlert", id ? "Listing updated successfully!" : "Surplus food listed successfully!");
      listingForm.reset();
      document.getElementById("listingId").value = "";
      resetSafeTimeInput();
      loadListings();
    } else {
      const err = await res.json();
      showAlert("listingAlert", "Error: " + (err.message || "Failed to save listing"), true);
    }
  } catch (err) {
    showAlert("listingAlert", "Network error connecting to backend.", true);
  }
});

document.getElementById("cancelListingEdit").addEventListener("click", () => {
  listingForm.reset();
  document.getElementById("listingId").value = "";
  resetSafeTimeInput();
});

function editListing(id) {
  const item = (window._listingsCache || []).find(l => l.id === id);
  if (!item) return;
  document.getElementById("listingId").value = item.id;
  document.getElementById("listingDonorId").value = item.donorId;
  document.getElementById("foodName").value = item.foodName;
  document.getElementById("foodType").value = item.foodType;
  document.getElementById("quantity").value = item.quantity;
  document.getElementById("unit").value = item.unit;
  if (item.safeToEatUntil) {
    document.getElementById("safeToEatUntil").value = item.safeToEatUntil.slice(0, 16);
  }
}

async function deleteListing(id) {
  if (!confirm(`Delete food listing #${id}?`)) return;
  await fetch(`${BASE_URL}/api/listings/${id}`, { method: "DELETE" });
  loadListings();
}

function quickClaim(listingId) {
  const claimTabBtn = document.querySelector('.tab-btn[data-tab="claims"]');
  if (claimTabBtn) claimTabBtn.click();
  setTimeout(() => {
    const select = document.getElementById("claimListingId");
    if (select) select.value = listingId;
  }, 150);
}

// ================= 2. DONORS =================
const donorForm = document.getElementById("donorForm");
const donorsTableBody = document.querySelector("#donorsTable tbody");

async function loadDonors() {
  try {
    const res = await fetch(`${BASE_URL}/api/donors`);
    const donors = await res.json();
    donorsTableBody.innerHTML = "";

    // Populate Donor dropdown in listing form
    const donorSelect = document.getElementById("listingDonorId");
    if (donorSelect) {
      donorSelect.innerHTML = '<option value="">-- Select Donor --</option>';
    }

    donors.forEach(d => {
      if (donorSelect) {
        const opt = document.createElement("option");
        opt.value = d.id;
        opt.textContent = `${d.name} (${d.phone})`;
        donorSelect.appendChild(opt);
      }

      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${d.id}</td>
        <td><strong>${d.name}</strong></td>
        <td>${d.email}</td>
        <td>${d.phone}</td>
        <td>${d.address}</td>
        <td>
          <button class="action edit-btn" onclick="editDonor(${d.id})">Edit</button>
          <button class="action delete-btn" onclick="deleteDonor(${d.id})">Delete</button>
        </td>`;
      donorsTableBody.appendChild(row);
    });
    window._donorsCache = donors;
  } catch (err) {
    console.error("Error loading donors", err);
  }
}

donorForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("donorId").value;
  const payload = {
    name: document.getElementById("donorName").value,
    email: document.getElementById("donorEmail").value,
    phone: document.getElementById("donorPhone").value,
    address: document.getElementById("donorAddress").value
  };

  try {
    let res;
    if (id) {
      res = await fetch(`${BASE_URL}/api/donors/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } else {
      res = await fetch(`${BASE_URL}/api/donors`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    }

    if (res.ok) {
      showAlert("donorAlert", id ? "Donor updated!" : "Donor saved successfully!");
      donorForm.reset();
      document.getElementById("donorId").value = "";
      loadDonors();
    } else {
      const err = await res.json();
      showAlert("donorAlert", "Error: " + (err.message || "Failed to save donor"), true);
    }
  } catch (err) {
    showAlert("donorAlert", "Network error.", true);
  }
});

document.getElementById("cancelDonorEdit").addEventListener("click", () => {
  donorForm.reset();
  document.getElementById("donorId").value = "";
});

function editDonor(id) {
  const donor = (window._donorsCache || []).find(d => d.id === id);
  if (!donor) return;
  document.getElementById("donorId").value = donor.id;
  document.getElementById("donorName").value = donor.name;
  document.getElementById("donorEmail").value = donor.email;
  document.getElementById("donorPhone").value = donor.phone;
  document.getElementById("donorAddress").value = donor.address;
}

async function deleteDonor(id) {
  if (!confirm(`Delete donor #${id}?`)) return;
  await fetch(`${BASE_URL}/api/donors/${id}`, { method: "DELETE" });
  loadDonors();
}

// ================= 3. NGOS =================
const ngoForm = document.getElementById("ngoForm");
const ngosTableBody = document.querySelector("#ngosTable tbody");

async function loadNGOs() {
  try {
    const res = await fetch(`${BASE_URL}/api/ngos`);
    const ngos = await res.json();
    ngosTableBody.innerHTML = "";

    // Populate NGO dropdown in claim form
    const claimNgoSelect = document.getElementById("claimNgoId");
    if (claimNgoSelect) {
      claimNgoSelect.innerHTML = '<option value="">-- Select Claiming NGO --</option>';
    }

    ngos.forEach(n => {
      if (claimNgoSelect) {
        const opt = document.createElement("option");
        opt.value = n.id;
        opt.textContent = `${n.name} (Contact: ${n.contactPerson})`;
        claimNgoSelect.appendChild(opt);
      }

      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${n.id}</td>
        <td><strong>${n.name}</strong></td>
        <td>${n.contactPerson}</td>
        <td>${n.email}</td>
        <td>${n.phone}</td>
        <td>${n.address}</td>
        <td>
          <button class="action edit-btn" onclick="editNGO(${n.id})">Edit</button>
          <button class="action delete-btn" onclick="deleteNGO(${n.id})">Delete</button>
        </td>`;
      ngosTableBody.appendChild(row);
    });
    window._ngosCache = ngos;
  } catch (err) {
    console.error("Error loading NGOs", err);
  }
}

ngoForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("ngoId").value;
  const payload = {
    name: document.getElementById("ngoName").value,
    contactPerson: document.getElementById("contactPerson").value,
    email: document.getElementById("ngoEmail").value,
    phone: document.getElementById("ngoPhone").value,
    address: document.getElementById("ngoAddress").value
  };

  try {
    let res;
    if (id) {
      res = await fetch(`${BASE_URL}/api/ngos/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } else {
      res = await fetch(`${BASE_URL}/api/ngos`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    }

    if (res.ok) {
      showAlert("ngoAlert", id ? "NGO updated!" : "NGO saved successfully!");
      ngoForm.reset();
      document.getElementById("ngoId").value = "";
      loadNGOs();
    } else {
      const err = await res.json();
      showAlert("ngoAlert", "Error: " + (err.message || "Failed to save NGO"), true);
    }
  } catch (err) {
    showAlert("ngoAlert", "Network error.", true);
  }
});

document.getElementById("cancelNgoEdit").addEventListener("click", () => {
  ngoForm.reset();
  document.getElementById("ngoId").value = "";
});

function editNGO(id) {
  const ngo = (window._ngosCache || []).find(n => n.id === id);
  if (!ngo) return;
  document.getElementById("ngoId").value = ngo.id;
  document.getElementById("ngoName").value = ngo.name;
  document.getElementById("contactPerson").value = ngo.contactPerson;
  document.getElementById("ngoEmail").value = ngo.email;
  document.getElementById("ngoPhone").value = ngo.phone;
  document.getElementById("ngoAddress").value = ngo.address;
}

async function deleteNGO(id) {
  if (!confirm(`Delete NGO #${id}?`)) return;
  await fetch(`${BASE_URL}/api/ngos/${id}`, { method: "DELETE" });
  loadNGOs();
}

// ================= 4. CLAIM / COLLECT =================
const claimForm = document.getElementById("claimForm");
const claimsTableBody = document.querySelector("#claimsTable tbody");

async function loadClaims() {
  try {
    const res = await fetch(`${BASE_URL}/api/claims`);
    const claims = await res.json();
    claimsTableBody.innerHTML = "";

    claims.forEach(c => {
      const row = document.createElement("tr");
      const isCollected = c.status === "COLLECTED";
      const claimedTime = new Date(c.claimedAt).toLocaleString([], { dateStyle: "short", timeStyle: "short" });
      const collectedTime = c.collectedAt ? new Date(c.collectedAt).toLocaleString([], { dateStyle: "short", timeStyle: "short" }) : "-";
      const badgeClass = isCollected ? "status-collected" : "status-claimed";

      row.innerHTML = `
        <td>${c.id}</td>
        <td>${c.listingId}</td>
        <td><strong>${c.foodName}</strong></td>
        <td>${c.quantity} ${c.unit}</td>
        <td>${c.ngoName}</td>
        <td>${claimedTime}</td>
        <td><span class="status-badge ${badgeClass}">${c.status}</span></td>
        <td>${collectedTime}</td>
        <td>
          ${!isCollected ? `<button class="action collect-btn" onclick="collectFood(${c.id})">Mark Collected</button>` : `<span style="color:#27ae60; font-weight:600;">✔ Picked Up</span>`}
        </td>`;
      claimsTableBody.appendChild(row);
    });
  } catch (err) {
    console.error("Error loading claims", err);
  }
}

claimForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const listingId = document.getElementById("claimListingId").value;
  const ngoId = document.getElementById("claimNgoId").value;

  if (!listingId || !ngoId) {
    alert("Please select both a food listing and an NGO.");
    return;
  }

  try {
    const res = await fetch(`${BASE_URL}/api/claims`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        listingId: parseInt(listingId, 10),
        ngoId: parseInt(ngoId, 10)
      })
    });

    if (res.ok) {
      showAlert("claimAlert", "Food claimed successfully by NGO!");
      claimForm.reset();
      loadClaims();
      loadListings();
    } else {
      const err = await res.json();
      showAlert("claimAlert", "Business Rule Rejection: " + (err.message || "Could not claim food"), true);
    }
  } catch (err) {
    showAlert("claimAlert", "Network error.", true);
  }
});

async function collectFood(claimId) {
  try {
    const res = await fetch(`${BASE_URL}/api/claims/${claimId}/collect`, { method: "PUT" });
    if (res.ok) {
      showAlert("claimAlert", "Food marked as COLLECTED and diverted from waste!");
      loadClaims();
      loadListings();
      loadAnalytics();
    } else {
      const err = await res.json();
      showAlert("claimAlert", "Error: " + (err.message || "Failed to mark collected"), true);
    }
  } catch (err) {
    showAlert("claimAlert", "Network error.", true);
  }
}

// ================= 5. WASTE DIVERSION & ANALYTICS =================
const analyticsTableBody = document.querySelector("#analyticsTable tbody");

async function loadAnalytics() {
  try {
    const reportRes = await fetch(`${BASE_URL}/api/analytics/monthly-diverted`);
    if (reportRes.ok) {
      const report = await reportRes.json();
      document.getElementById("statTotalQuantity").textContent = report.totalQuantityDiverted.toFixed(1);
      document.getElementById("statTotalListings").textContent = report.totalListingsDiverted;
      document.getElementById("statSummaryMonth").textContent = `${report.month} / ${report.year}`;
    }

    const claimsRes = await fetch(`${BASE_URL}/api/claims`);
    if (claimsRes.ok) {
      const allClaims = await claimsRes.json();
      const collected = allClaims.filter(c => c.status === "COLLECTED");
      analyticsTableBody.innerHTML = "";

      collected.forEach(c => {
        const row = document.createElement("tr");
        const collectedTime = c.collectedAt ? new Date(c.collectedAt).toLocaleString() : "-";
        row.innerHTML = `
          <td>${c.id}</td>
          <td>${c.listingId}</td>
          <td><strong>${c.foodName}</strong></td>
          <td><strong>${c.quantity} ${c.unit}</strong></td>
          <td>${c.ngoName}</td>
          <td>${collectedTime}</td>
          <td><span class="status-badge status-collected">Diverted From Waste</span></td>`;
        analyticsTableBody.appendChild(row);
      });
    }
  } catch (err) {
    console.error("Error loading analytics", err);
  }
}

document.getElementById("refreshAnalytics").addEventListener("click", loadAnalytics);

// ---------- Initial Load ----------
resetSafeTimeInput();
loadDonors();
loadNGOs();
loadListings();
