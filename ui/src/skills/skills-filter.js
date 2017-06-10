import {bindable, bindingMode, inject,BindingEngine} from 'aurelia-framework';

const concat = (xs,y) => xs.concat(y)
const flatMap = (f,xs) => xs.map(f).reduce(concat, [])

@inject(BindingEngine)
export class SkillsFilterCustomElement {
  @bindable({ defaultBindingMode: bindingMode.twoWay }) skills = [];
  selectedFilters = [];
  allSkills = [];
  tags = [];
  activeTags = [];

  constructor(bindingEngine) {
    this.subscription = bindingEngine.propertyObserver(this, 'skills')
      .subscribe((newValue, oldValue) => this.init(newValue));
  }
  init(skills) {
    if(skills && skills.length) {
      this.allSkills = skills
      this.tags = flatMap(s => s.tags, this.allSkills).filter((value, index, self) => self.indexOf(value) === index);
      this.subscription.dispose();
    }
  }
  attached() {
    if(this.skills && this.skills.length) this.init(this.skills);
  }
  isActive(tag) {
    return this.activeTags.indexOf(tag) > -1;
  }
  toggleTag(tag) {
    let index = this.activeTags.indexOf(tag)
    if(index === -1) {
      this.activeTags.push(tag)
    } else {
      this.activeTags.splice(index, 1)
    }
    this.refresh();
  }
  refresh() {
    this.skills = this.allSkills.filter(s => this.activeTags.length === 0 || this.activeTags.some(t => s.tags.indexOf(t) > -1));
  }
}
