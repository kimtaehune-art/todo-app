// カテゴリー追加: 選択中のカラーをプレビュー (option の data-rgb を見本に反映)
// 非同期通信なし。選択値を視覚化するだけのクライアント補助。
document.addEventListener('DOMContentLoaded', function () {
  var select  = document.querySelector('.js-color-select');
  var preview = document.querySelector('.js-color-preview');
  if (!select || !preview) return;

  function update() {
    var opt = select.options[select.selectedIndex];
    preview.style.backgroundColor = opt ? opt.dataset.rgb : '';
  }

  select.addEventListener('change', update);
  update(); // 初期表示 (fill された値 or 先頭) を反映
});
