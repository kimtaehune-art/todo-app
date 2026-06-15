// Todo一覧: 削除確認のカスタムモーダル
// ネイティブ confirm() の代わりに <dialog> を使う。
// 非同期リクエストはせず、確認後にフォームを通常どおり submit する (MPA のまま)。
document.addEventListener('DOMContentLoaded', function () {
  var dialog = document.getElementById('js-delete-dialog');
  if (!dialog) return;

  var confirmBtn = dialog.querySelector('.js-delete-confirm');
  var cancelBtn  = dialog.querySelector('.js-delete-cancel');
  var pendingForm = null;

  // 各削除フォームの submit を横取りしてモーダルを表示する
  document.querySelectorAll('.js-delete-form').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      pendingForm = form;
      dialog.showModal();
    });
  });

  // 「削除」: モーダルを閉じてフォームを送信 (form.submit() は submit リスナーを発火しない)
  confirmBtn.addEventListener('click', function () {
    dialog.close();
    if (pendingForm) pendingForm.submit();
  });

  // 「キャンセル」: 何もせず閉じる
  cancelBtn.addEventListener('click', function () {
    dialog.close();
    pendingForm = null;
  });
});
