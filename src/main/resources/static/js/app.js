const state = { worlds: [], world: null, items: [], links: [], filter: "all", search: "", view: "grid", detailId: null };
const kindIcons = { realm: "⌂", character: "♙", creature: "✿", quest: "✧", relic: "◇" };
const kindLabels = { realm: "Realm", character: "Character", creature: "Creature", quest: "Quest", relic: "Relic" };
const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

function escapeHtml(value) {
  return String(value ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}
function showToast(message) {
  const toast = $("#toast");
  toast.textContent = message;
  toast.classList.add("show");
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => toast.classList.remove("show"), 2200);
}
async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
  });
  if (response.status === 204) return null;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.detail || data.message || "That request could not be completed.");
  return data;
}
function openModal(id) { $(`#${id}`).showModal(); }
function closeModal(id) { $(`#${id}`).close(); }
function formatDate(value) {
  if (!value) return "JUST CREATED";
  return new Intl.DateTimeFormat(undefined, { month: "short", year: "numeric" }).format(new Date(value));
}
function setWorkspace(data) {
  state.world = data.world;
  state.items = data.items || [];
  state.links = data.links || [];
  localStorage.setItem("mythos-forge:world", String(state.world.id));
  renderAll();
}
async function loadWorkspace(worldId) {
  try { setWorkspace(await api(`/worlds/${worldId}/workspace`)); }
  catch (error) { showToast(error.message); }
}
async function loadWorlds(selectId = null) {
  state.worlds = await api("/worlds");
  renderWorldList();
  if (!state.worlds.length) { openModal("world-modal"); return; }
  const savedId = selectId ?? Number(localStorage.getItem("mythos-forge:world"));
  const selected = state.worlds.find((world) => world.id === savedId) || state.worlds[0];
  await loadWorkspace(selected.id);
}
function renderWorldList() {
  const list = $("#world-list");
  list.innerHTML = state.worlds.map((world) => {
    const selected = state.world?.id === world.id;
    return `<button class="world-row ${selected ? "selected" : ""}" data-world="${world.id}"><span class="world-glyph">✺</span><span class="world-row-text"><strong>${escapeHtml(world.name)}</strong><small>${escapeHtml(world.genre)}</small></span></button>`;
  }).join("");
  $$(".world-row", list).forEach((button) => button.addEventListener("click", () => loadWorkspace(Number(button.dataset.world))));
}
function renderHero() {
  const world = state.world;
  if (!world) return;
  $("#breadcrumb-world").textContent = world.name.toUpperCase();
  $("#world-title").textContent = world.name;
  $("#world-genre").textContent = world.genre;
  $("#world-premise").textContent = world.premise;
  $("#world-seed").textContent = world.seed;
  $("#world-created").textContent = formatDate(world.createdAt);
  $("#generate-world").disabled = state.items.length > 0;
  $("#generate-world").title = state.items.length ? "Starter lore has already been generated for this world" : "Generate a seeded set of realms, characters, creatures, quests, relics and links";
}
function renderStats() {
  const counts = Object.fromEntries(Object.keys(kindLabels).map((kind) => [kind, state.items.filter((item) => item.kind === kind).length]));
  Object.entries(counts).forEach(([kind, count]) => { $(`#stat-${kind}`).textContent = count; $(`#count-${kind}`).textContent = count; });
  $("#stat-links").textContent = state.links.length;
  $("#count-all").textContent = state.items.length;
  $("#entry-total").textContent = `${state.items.length} ${state.items.length === 1 ? "entry" : "entries"}`;
}
function visibleItems() {
  const query = state.search.trim().toLowerCase();
  return state.items.filter((item) => (state.filter === "all" || item.kind === state.filter)
    && (!query || [item.name, item.subtitle, item.description, item.traits].some((part) => part.toLowerCase().includes(query))));
}
function renderCards() {
  const grid = $("#entry-grid");
  $("#connection-map").classList.toggle("hidden", state.view !== "map");
  grid.classList.toggle("hidden", state.view === "map");
  $$(".icon-toggle").forEach((button) => button.classList.toggle("active", button.id === (state.view === "grid" ? "grid-view" : "map-view")));
  if (state.view === "map") { renderMap(); return; }
  const entries = visibleItems();
  if (!entries.length) {
    const generated = state.items.length > 0;
    grid.innerHTML = `<div class="empty-state"><div><div class="empty-sigil">${generated ? "⌕" : "✺"}</div><h3>${generated ? "No entries found." : "An empty world awaits."}</h3><p>${generated ? "Try another search or choose a different kind of story." : "Generate a starter collection of realms, characters and quests, or begin with a detail of your own."}</p>${generated ? "" : `<button class="button button-gold" data-empty-generate>✧ Generate starter lore</button>`}</div></div>`;
    $("[data-empty-generate]")?.addEventListener("click", generateWorld);
    return;
  }
  grid.innerHTML = entries.map((item) => {
    const traits = item.traits.split(/[·,]/).map((trait) => trait.trim()).filter(Boolean).slice(0, 4);
    const related = state.links.filter((link) => link.sourceId === item.id || link.targetId === item.id).length;
    return `<article class="entry-card ${escapeHtml(item.kind)}" data-entry="${item.id}" tabindex="0" role="button" aria-label="Open ${escapeHtml(item.name)}">
      <div class="entry-card-top"><span class="kind-badge ${escapeHtml(item.kind)}">${kindIcons[item.kind] || "✦"} ${kindLabels[item.kind] || "Entry"}</span><button class="card-menu" data-edit="${item.id}" aria-label="Edit ${escapeHtml(item.name)}" title="Edit entry">⋯</button></div>
      <div class="entry-symbol">${kindIcons[item.kind] || "✦"}</div><h3>${escapeHtml(item.name)}</h3><div class="entry-subtitle">${escapeHtml(item.subtitle)}</div><p class="entry-description">${escapeHtml(item.description)}</p>
      <div class="trait-list">${traits.map((trait) => `<span class="trait">${escapeHtml(trait)}</span>`).join("")}</div><div class="card-footer"><span>CREATED ${formatDate(item.createdAt)}</span><span class="card-links">⌁ ${related} ${related === 1 ? "LINK" : "LINKS"}</span></div>
    </article>`;
  }).join("");
  $$(".entry-card", grid).forEach((card) => {
    card.addEventListener("click", () => openDetail(Number(card.dataset.entry)));
    card.addEventListener("keydown", (event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); openDetail(Number(card.dataset.entry)); } });
  });
  $$("[data-edit]", grid).forEach((button) => button.addEventListener("click", (event) => { event.stopPropagation(); openEntryForm(state.items.find((item) => item.id === Number(button.dataset.edit))); }));
}
function renderMap() {
  const mount = $("#connection-map");
  if (!state.items.length) {
    mount.innerHTML = `<div class="map-empty"><div><strong>Nothing is connected yet.</strong>Add entries and create relationships to see your world take shape.</div></div>`;
    return;
  }
  const width = 920, height = 390, cx = width / 2, cy = height / 2;
  const radius = Math.min(140, 34 + state.items.length * 8);
  const positions = new Map();
  state.items.forEach((item, index) => {
    const angle = (Math.PI * 2 * index / state.items.length) - Math.PI / 2;
    positions.set(item.id, { x: cx + Math.cos(angle) * radius, y: cy + Math.sin(angle) * radius });
  });
  const edgeMarkup = state.links.map((link) => {
    const from = positions.get(link.sourceId), to = positions.get(link.targetId);
    if (!from || !to) return "";
    const mx = (from.x + to.x) / 2, my = (from.y + to.y) / 2;
    return `<g><line class="map-edge" x1="${from.x}" y1="${from.y}" x2="${to.x}" y2="${to.y}"/><text class="map-edge-label" x="${mx}" y="${my - 4}">${escapeHtml(link.label)}</text></g>`;
  }).join("");
  const nodes = state.items.map((item) => {
    const { x, y } = positions.get(item.id);
    const label = item.name.length > 19 ? `${item.name.slice(0, 17)}…` : item.name;
    return `<g class="map-node" data-node="${item.id}" role="button" tabindex="0"><circle cx="${x}" cy="${y}" r="22"/><text x="${x}" y="${y + 4}" style="font-size:14px">${kindIcons[item.kind] || "✦"}</text><text x="${x}" y="${y + 39}">${escapeHtml(label)}</text></g>`;
  }).join("");
  mount.innerHTML = `<svg class="map-svg" viewBox="0 0 ${width} ${height}" role="img" aria-label="Map of connected world entries">${edgeMarkup}<circle cx="${cx}" cy="${cy}" r="34" fill="#2d291f" stroke="#75613d"/><text x="${cx}" y="${cy + 5}" fill="#e4cb8c" text-anchor="middle" font-size="18">✺</text>${nodes}</svg>`;
  $$("[data-node]", mount).forEach((node) => {
    node.addEventListener("click", () => openDetail(Number(node.dataset.node)));
    node.addEventListener("keydown", (event) => { if (event.key === "Enter") openDetail(Number(node.dataset.node)); });
  });
}
function renderAll() {
  renderWorldList();
  renderHero();
  renderStats();
  renderCards();
}
function relationDescription(itemId) {
  const item = state.items.find((entry) => entry.id === itemId);
  return state.links.filter((link) => link.sourceId === itemId || link.targetId === itemId).map((link) => {
    const outgoing = link.sourceId === itemId;
    const other = state.items.find((entry) => entry.id === (outgoing ? link.targetId : link.sourceId));
    if (!other) return "";
    return `<p>${escapeHtml(outgoing ? link.label : `is linked by “${link.label}” to`)} <strong>${escapeHtml(other.name)}</strong></p>`;
  }).join("") || `<p>No connections yet. Use “Connect entries” to add a story thread.</p>`;
}
function openDetail(itemId) {
  const item = state.items.find((entry) => entry.id === itemId);
  if (!item) return;
  state.detailId = itemId;
  const content = $("#detail-content");
  const traits = item.traits.split(/[·,]/).map((trait) => trait.trim()).filter(Boolean);
  content.innerHTML = `<div class="detail-hero"><button class="detail-close" data-detail-close aria-label="Close">×</button>${kindIcons[item.kind] || "✦"}</div><div class="detail-content"><span class="kind-badge ${escapeHtml(item.kind)}">${kindLabels[item.kind] || "Entry"}</span><h2>${escapeHtml(item.name)}</h2><p class="detail-subtitle">${escapeHtml(item.subtitle)}</p><p class="detail-description">${escapeHtml(item.description)}</p><div class="detail-traits">${traits.map((trait) => `<span class="trait">${escapeHtml(trait)}</span>`).join("")}</div><div class="detail-links"><h3>STORY THREADS</h3>${relationDescription(item.id)}</div><div class="detail-actions"><button class="button button-quiet" data-delete-entry="${item.id}">Delete entry</button><button class="button button-outline" data-edit-entry="${item.id}">Edit entry</button><button class="button button-gold" data-detail-close>Close</button></div></div>`;
  openModal("detail-modal");
  $$('[data-detail-close]', content).forEach((button) => button.addEventListener("click", () => closeModal("detail-modal")));
  $("[data-edit-entry]", content).addEventListener("click", () => { closeModal("detail-modal"); openEntryForm(item); });
  $("[data-delete-entry]", content).addEventListener("click", () => deleteEntry(item.id));
}
function openEntryForm(item = null) {
  const form = $("#entry-form");
  form.reset();
  form.elements.id.value = item?.id || "";
  $("#entry-modal-title").textContent = item ? "Edit world entry" : "New world entry";
  form.elements.kind.value = item?.kind || "realm";
  form.elements.name.value = item?.name || "";
  form.elements.subtitle.value = item?.subtitle || "";
  form.elements.description.value = item?.description || "";
  form.elements.traits.value = item?.traits || "";
  openModal("entry-modal");
}
async function generateWorld() {
  try {
    setWorkspace(await api(`/worlds/${state.world.id}/generate`, { method: "POST" }));
    showToast("A starter tapestry of lore has been woven.");
  } catch (error) { showToast(error.message); }
}
async function deleteEntry(itemId) {
  const item = state.items.find((entry) => entry.id === itemId);
  if (!item || !window.confirm(`Remove “${item.name}” and its connections from this world?`)) return;
  try {
    await api(`/worlds/${state.world.id}/items/${itemId}`, { method: "DELETE" });
    closeModal("detail-modal");
    await loadWorkspace(state.world.id);
    showToast("Entry removed from the world.");
  } catch (error) { showToast(error.message); }
}
function fillLinkForm() {
  const form = $("#link-form");
  const options = state.items.map((item) => `<option value="${item.id}">${escapeHtml(item.name)} · ${escapeHtml(kindLabels[item.kind])}</option>`).join("");
  form.elements.sourceId.innerHTML = options;
  form.elements.targetId.innerHTML = options;
  form.elements.sourceId.disabled = state.items.length < 2;
  form.elements.targetId.disabled = state.items.length < 2;
  $("#link-form button[type=submit]").disabled = state.items.length < 2;
}

$("#open-world-modal").addEventListener("click", () => openModal("world-modal"));
$("#open-world-modal-secondary").addEventListener("click", () => openModal("world-modal"));
$("#add-entry-button").addEventListener("click", () => openEntryForm());
$("#add-link-button").addEventListener("click", () => { fillLinkForm(); if (state.items.length < 2) { showToast("Add at least two entries before connecting them."); return; } openModal("link-modal"); });
$("#generate-world").addEventListener("click", generateWorld);
$("#grid-view").addEventListener("click", () => { state.view = "grid"; renderCards(); });
$("#map-view").addEventListener("click", () => { state.view = "map"; renderCards(); });
$("#search-input").addEventListener("input", (event) => { state.search = event.target.value; if (state.view === "grid") renderCards(); });
$("#filter-tabs").addEventListener("click", (event) => {
  const button = event.target.closest("[data-filter]");
  if (!button) return;
  state.filter = button.dataset.filter;
  $$(".filter-tab").forEach((tab) => tab.classList.toggle("active", tab === button));
  if (state.view === "grid") renderCards();
});
$("#world-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const body = Object.fromEntries(new FormData(form));
  body.seed = body.seed ? Number(body.seed) : null;
  try {
    const world = await api("/worlds", { method: "POST", body: JSON.stringify(body) });
    form.reset(); closeModal("world-modal"); await loadWorlds(world.id); showToast("Your new world has been forged.");
  } catch (error) { showToast(error.message); }
});
$("#entry-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = Object.fromEntries(new FormData(form));
  const id = values.id;
  delete values.id;
  try {
    await api(`/worlds/${state.world.id}/items${id ? `/${id}` : ""}`, { method: id ? "PUT" : "POST", body: JSON.stringify(values) });
    closeModal("entry-modal"); await loadWorkspace(state.world.id); showToast(id ? "World entry updated." : "A new story has found its place.");
  } catch (error) { showToast(error.message); }
});
$("#link-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(event.currentTarget));
  values.sourceId = Number(values.sourceId); values.targetId = Number(values.targetId);
  try {
    await api(`/worlds/${state.world.id}/links`, { method: "POST", body: JSON.stringify(values) });
    closeModal("link-modal"); await loadWorkspace(state.world.id); showToast("The connection has been woven into the map.");
  } catch (error) { showToast(error.message); }
});
$("#export-world").addEventListener("click", () => {
  if (!state.world) return;
  const data = { world: state.world, entries: state.items, connections: state.links };
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob); link.download = `${state.world.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "") || "mythos-world"}.json`;
  link.click(); URL.revokeObjectURL(link.href); showToast("World exported as a JSON archive.");
});
$$('[data-close]').forEach((button) => button.addEventListener("click", () => closeModal(button.dataset.close)));
$$('dialog').forEach((dialog) => dialog.addEventListener("click", (event) => { if (event.target === dialog) dialog.close(); }));

loadWorlds().catch((error) => showToast(error.message || "Could not connect to the world archive."));
